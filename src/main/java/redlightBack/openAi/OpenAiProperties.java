package redlightBack.openAi;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.stereotype.Component;

@Component
public class OpenAiProperties {

        private final String apiKey;

        public OpenAiProperties() {
            Dotenv dotenv = Dotenv.configure().load();
            this.apiKey = dotenv.get("OPENAI_API_KEY");
        }

        public String getApiKey() {
            return apiKey;
        }
}

