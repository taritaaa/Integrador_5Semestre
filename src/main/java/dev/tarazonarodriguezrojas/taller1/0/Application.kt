package dev.tarazonarodriguezrojas.taller1.`0`

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication


@SpringBootApplication
object Application {
    @JvmStatic
    fun main(args: Array<String>) {
        SpringApplication.run(Application::class.java, *args)
    }
}
