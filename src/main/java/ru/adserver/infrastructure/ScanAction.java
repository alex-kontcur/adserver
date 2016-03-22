package ru.adserver.infrastructure;

/**
 * ScanAction
 *
 * @author Kontsur Alex (bona)
 * @since 13.11.2015
 */
public interface ScanAction {

    void execute(String targetPath, String fileName);

    String description();

}
