package com.upt.cti.smartwallet;

public enum Month {
    January, February, March, April, May, June, July, August, September, October, November, December;

    public static int monthNameToInt(java.time.Month month) {
        return month.ordinal();
    }

    public static java.time.Month intToMonthName(int index) {
        return java.time.Month.values()[index];
    }

    public static int monthFromTimestamp(String timestamp) {
        // 2016-11-02 14:15:16
        int month = Integer.parseInt(timestamp.substring(5, 7));
        return month - 1;
    }


}
