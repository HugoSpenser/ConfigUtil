package props;

import config.ConfigFileName;
import config.Properties;

@ConfigFileName("application")
public enum Spring implements Properties<Spring> {
    LOG_FILE("logging.file"),
    ;

    Spring(String propName) {
        this.propName = propName;
    }

    private String propName;

    @Override
    public String getPropName() {
        return propName;
    }

    @Override
    public boolean isRequired() {
        return false;
    }
}
