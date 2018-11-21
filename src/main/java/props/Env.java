package props;


import config.ConfigFileName;
import config.Properties;

@ConfigFileName("")
public enum Env implements Properties<Env> {

    DB_HOST("db.host", true),
    ;

    Env(String propName, boolean required) {
        this.propName = propName;
        this.required = required;
    }

    private String propName;
    private boolean required;

    @Override
    public String getPropName() {
        return propName;
    }

    @Override
    public boolean isRequired() {
        return required;
    }
}
