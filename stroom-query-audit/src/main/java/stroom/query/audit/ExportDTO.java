package stroom.query.audit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExportDTO {
    private Map<String, String> values;
    private List<String> messages;

    public ExportDTO() {
        this.values = new HashMap<>();
        this.messages = new ArrayList<>();
    }

    public Map<String, String> getValues() {
        return values;
    }

    public void setValues(Map<String, String> values) {
        this.values = values;
    }

    public List<String> getMessages() {
        return messages;
    }

    public void setMessages(final List<String> messages) {
        this.messages = messages;
    }

    public static Builder withMessage(final String message) {
        return new Builder().message(message);
    }

    public static Builder withValue(final String key, final String value) {
        return new Builder().value(key, value);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ExportDTO{");
        sb.append("values=").append(values);
        sb.append(", messages=").append(messages);
        sb.append('}');
        return sb.toString();
    }

    public static class Builder {
        private final ExportDTO instance;

        public Builder() {
            this.instance = new ExportDTO();
        }

        public Builder message(final String value) {
            this.instance.messages.add(value);
            return this;
        }

        public Builder value(final String key, final String value) {
            this.instance.values.put(key, value);
            return this;
        }

        public Builder values(final Map<String, Object> values) {
            values.forEach((k, v) -> {
                if (v != null) {
                    this.value(k, v.toString());
                }
            });
            return this;
        }

        public ExportDTO build() {
            return instance;
        }
    }
}
