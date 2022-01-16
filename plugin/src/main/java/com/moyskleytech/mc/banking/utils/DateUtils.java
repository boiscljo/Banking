package com.moyskleytech.mc.banking.utils;
import org.screamingsandals.lib.plugin.ServiceManager;
import org.screamingsandals.lib.utils.annotations.Service;

import java.text.SimpleDateFormat;
import java.util.Date;

@Service(dependsOn = {

})
public class DateUtils {

    public static DateUtils getInstance() {
        return ServiceManager.get(DateUtils.class);
    }

    private final SimpleDateFormat format;

    public DateUtils() {
        format = new SimpleDateFormat(("MM/dd/yy"));
    }

    public static SimpleDateFormat getSimpleDateFormat() {
        return getInstance().format;
    }

    public static String getFormattedDate() {
        return getSimpleDateFormat().format(new Date());
    }
}
