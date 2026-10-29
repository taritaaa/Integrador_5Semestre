package dev.tarazonarodriguezrojas.taller1.service;

import dev.tarazonarodriguezrojas.taller1.model.dto.PartDTO;
import dev.tarazonarodriguezrojas.taller1.model.entity.Part;
import dev.tarazonarodriguezrojas.taller1.model.entity.VehicleModel;
import dev.tarazonarodriguezrojas.taller1.repository.PartRepository;
import dev.tarazonarodriguezrojas.taller1.repository.VehicleModelRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

@Service
@Transactional
public class PartService {

    // Imagen de referencia: tipos permitidos (JPG, PNG, WEBP) y tamaño máximo (2 MB)
    private static final long MAX_IMAGE_SIZE = 2L * 1024 * 1024;
    private static final Map<String, String> IMAGE_TYPES = Map.of(
            "image/jpeg", "jpg",
            "image/png", "png",
            "image/webp", "webp");

    // La columna price es DECIMAL(10,2): máximo 8 dígitos enteros y 2 decimales
    private static final BigDecimal MAX_PRICE = new BigDecimal("99999999.99");

    // Código de barras EAN-13: exactamente 13 dígitos
    private static final Pattern EAN_PATTERN = Pattern.compile("^\\d{13}$");

    private final PartRepository repository;
    private final VehicleModelRepository modelRepository;
    private final FileStorageService fileStorage;

    public PartService(PartRepository repository, VehicleModelRepository modelRepository,
                       FileStorageService fileStorage) {
        this.repository = repository;
        this.modelRepository = modelRepository;
        this.fileStorage = fileStorage;
    }

    // RF-17: listado de repuestos
    @Transactional(readOnly = true)
    public List<PartDTO> findAllParts() {
        return repository.findAll().stream().map(this::toDTO).toList();
    }

    // RF-17: detalle de un repuesto
    @Transactional(readOnly = true)
    public PartDTO findPartById(Integer partId) throws EntityNotFoundException {
        return toDTO(findEntity(partId));
    }

    // RF-26: repuestos compatibles con un modelo (con su precio y stock)
    @Transactional(readOnly = true)
    public List<PartDTO> findCompatibleParts(Integer modelId)
            throws EntityNotFoundException, IllegalArgumentException {
        findModel(modelId);
        return repository.findByModelId(modelId).stream().map(this::toDTO).toList();
    }

    // RF-16: registrar repuesto. La imagen es opcional; también se puede subir después (RF-20).
    public PartDTO createPart(PartDTO request, MultipartFile image)
            throws EntityNotFoundException, IllegalArgumentException {
        validate(request);
        validateEan(request.ean());

        String ean = request.ean().trim();
        if (repository.existsByEan(ean)) {
            throw new IllegalArgumentException("Ya existe un repuesto con el código de barras " + ean);
        }

        VehicleModel model = findModel(request.modelId());

        // La imagen se valida antes de guardar nada, para no dejar un repuesto a medias
        String extension = hasImage(image) ? validateImage(image) : null;

        Part part = new Part();
        part.setEan(ean);
        setAttributesFromDTO(request, part, model);
        if (extension != null) {
            part.setImageUrl(fileStorage.store(image, extension));
        }

        return toDTO(repository.save(part));
    }

    // RF-18: actualizar repuesto (incluye precio y stock). El código de barras NO se modifica.
    public PartDTO updatePart(Integer partId, PartDTO request)
            throws EntityNotFoundException, IllegalArgumentException {
        Part part = findEntity(partId);
        validate(request);

        VehicleModel model = findModel(request.modelId());
        setAttributesFromDTO(request, part, model);

        return toDTO(repository.save(part));
    }

    // RF-19: eliminar repuesto. Si está asociado a reparaciones, no se elimina.
    public void deletePart(Integer partId) throws EntityNotFoundException, IllegalStateException {
        Part part = findEntity(partId);
        if (!part.getRepairParts().isEmpty()) {
            throw new IllegalStateException("No se puede eliminar el repuesto " + part.getName()
                    + " porque está asociado a reparaciones");
        }
        String imageFile = part.getImageUrl();
        repository.delete(part);
        fileStorage.delete(imageFile);
    }

    // RF-20: adjuntar o reemplazar la imagen de referencia de un repuesto
    public PartDTO saveImage(Integer partId, MultipartFile image)
            throws EntityNotFoundException, IllegalArgumentException {
        Part part = findEntity(partId);
        if (!hasImage(image)) {
            throw new IllegalArgumentException("Debe seleccionar una imagen");
        }
        String extension = validateImage(image);

        String oldFile = part.getImageUrl();
        part.setImageUrl(fileStorage.store(image, extension));
        Part saved = repository.save(part);

        // Al reemplazar, se borra la imagen anterior para no acumular archivos
        fileStorage.delete(oldFile);
        return toDTO(saved);
    }
    // RF-25: descuenta del stock la cantidad usada en una reparación.
    // Si no hay stock suficiente, lanza IllegalStateException y la reparación no se registra.
    public void decreaseStock(Integer partId, Integer quantity)
            throws EntityNotFoundException, IllegalStateException {
        Part part = findEntity(partId);
        if (part.getStock() < quantity) {
            throw new IllegalStateException("Stock insuficiente del repuesto " + part.getName()
                    + ": hay " + part.getStock() + " y se necesitan " + quantity);
        }
        part.setStock(part.getStock() - quantity);
        repository.save(part);
    }

    // RF-25: devuelve al stock la cantidad de una reparación cancelada o eliminada
    public void restoreStock(Integer partId, Integer quantity) throws EntityNotFoundException {
        Part part = findEntity(partId);
        part.setStock(part.getStock() + quantity);
        repository.save(part);
    }

    private Part findEntity(Integer partId) {
        return repository.findById(partId)
                .orElseThrow(() -> new EntityNotFoundException("Repuesto con id " + partId + " no encontrado"));
    }

    private VehicleModel findModel(Integer modelId) {
        if (modelId == null) {
            throw new IllegalArgumentException("Debe seleccionar un modelo");
        }
        return modelRepository.findById(modelId)
                .orElseThrow(() -> new EntityNotFoundException("Modelo con id " + modelId + " no encontrado"));
    }

    // Copia los datos del DTO a la entidad (todos menos el EAN y la imagen)
    private void setAttributesFromDTO(PartDTO request, Part part, VehicleModel model) {
        part.setName(request.name().trim());
        part.setDescription(request.description() == null || request.description().isBlank()
                ? null : request.description().trim());
        part.setPrice(request.price().setScale(2, RoundingMode.HALF_UP));
        part.setStock(request.stock());
        part.setModel(model);
    }

    private PartDTO toDTO(Part part) {
        return new PartDTO(
                part.getId(),
                part.getName(),
                part.getDescription(),
                part.getPrice(),
                part.getEan(),
                part.getStock(),
                part.getImageUrl(),
                part.getModel().getId(),
                part.getModel().getName(),
                part.getModel().getBrand());
    }

    private boolean hasImage(MultipartFile image) {
        return image != null && !image.isEmpty();
    }

    // Valida tipo y tamaño de la imagen y devuelve la extensión con la que se guardará
    private String validateImage(MultipartFile image) {
        String contentType = image.getContentType() == null ? "" : image.getContentType().toLowerCase();
        String extension = IMAGE_TYPES.get(contentType);
        if (extension == null) {
            throw new IllegalArgumentException("La imagen debe ser JPG, PNG o WEBP");
        }
        if (image.getSize() > MAX_IMAGE_SIZE) {
            throw new IllegalArgumentException("La imagen no puede superar 2 MB");
        }
        return extension;
    }

    private void validateEan(String ean) {
        if (ean == null || ean.isBlank()) {
            throw new IllegalArgumentException("El código de barras (EAN) es obligatorio");
        }
        if (!EAN_PATTERN.matcher(ean.trim()).matches()) {
            throw new IllegalArgumentException("El código de barras debe tener exactamente 13 dígitos");
        }
    }

    // Validaciones de los datos que no dependen de la base de datos
    private void validate(PartDTO request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new IllegalArgumentException("El nombre del repuesto es obligatorio");
        }
        if (request.name().trim().length() > 100) {
            throw new IllegalArgumentException("El nombre no puede superar 100 caracteres");
        }
        if (request.description() != null && request.description().trim().length() > 255) {
            throw new IllegalArgumentException("La descripción no puede superar 255 caracteres");
        }
        if (request.price() == null) {
            throw new IllegalArgumentException("El precio es obligatorio");
        }
        if (request.price().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El precio no puede ser negativo");
        }
        if (request.price().compareTo(MAX_PRICE) > 0) {
            throw new IllegalArgumentException("El precio no puede superar 99.999.999,99");
        }
        if (request.stock() == null) {
            throw new IllegalArgumentException("El stock es obligatorio");
        }
        if (request.stock() < 0) {
            throw new IllegalArgumentException("El stock no puede ser negativo");
        }
    }
}