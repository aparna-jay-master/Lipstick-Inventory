package com.example.android.lipstickinventory;

/**
 * Creates Search object with has
 * title, snippet, and webURL
 */

public class Search {

    //title
    private String mTitle;

    //snippet
    private String mSnippet;

    //webURL
    private String mWebUrl;

    /**
     * Constructor for a new {@link Search} object
     *
     * @param title title
     * @param snippet snippet
     * @param webUrl url
     */
    public Search(String title, String snippet, String webUrl) {
        mTitle = title;
        mSnippet = snippet;
        mWebUrl = webUrl;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getSnippet() {
        return mSnippet;
    }

    public String getWebUrl() {
        return mWebUrl;
    }
}
