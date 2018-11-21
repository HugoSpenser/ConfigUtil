package config;

public interface Properties<V extends Properties> {
    String getPropName();
    boolean isRequired();
}
