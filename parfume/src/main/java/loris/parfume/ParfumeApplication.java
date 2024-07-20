package loris.parfume;

import jakarta.annotation.PostConstruct;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.web.config.EnableSpringDataWebSupport;

import java.util.TimeZone;

import static org.springframework.data.web.config.EnableSpringDataWebSupport.PageSerializationMode.VIA_DTO;

@SpringBootApplication
@EnableSpringDataWebSupport(pageSerializationMode = VIA_DTO)
public class ParfumeApplication {

	public static void main(String[] args) {
		SpringApplication.run(ParfumeApplication.class, args);
	}

	@PostConstruct
	public void init() {

		TimeZone.setDefault(TimeZone.getTimeZone("Asia/Tashkent"));
	}
}