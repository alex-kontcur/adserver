package ru.adserver.infrastructure.hibernate;

import org.jadira.usertype.dateandtime.joda.PersistentDateTime;
import org.joda.time.DateTimeZone;

import java.util.Properties;

/**
 * DefaultPersistentDateTime
 *
 * @author Kontsur Alex (bona)
 * @since 14.11.2015
 */
public class DefaultPersistentDateTime extends PersistentDateTime {

    private static final long serialVersionUID = 5272756952065762564L;

    private static final String P_DATABASE_ZONE = "databaseZone";

    @Override
    public void setParameterValues(Properties parameters) {
        Properties props = parameters;
        if (props == null) {
            props = new Properties();
        }
        props.setProperty(P_DATABASE_ZONE, DateTimeZone.getDefault().getID());
        super.setParameterValues(props);
    }
}
