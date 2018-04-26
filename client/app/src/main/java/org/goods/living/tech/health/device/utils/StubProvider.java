package org.goods.living.tech.health.device.utils;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class StubProvider extends ContentProvider {


    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + AuthenticatorService.CONTENT_AUTHORITY);
    public static final int ROUTE_ENTRIES = 1;
    public static final int ROUTE_ENTRIES_ID = 2;
    public static final String CONTENT_TYPE =
            ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.android.entries";
    /**
     * MIME type for individual entries.
     */
    public static final String CONTENT_ITEM_TYPE =
            ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.android.entry";
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(AuthenticatorService.CONTENT_AUTHORITY, "entries", ROUTE_ENTRIES);
        sUriMatcher.addURI(AuthenticatorService.CONTENT_AUTHORITY, "entries/*", ROUTE_ENTRIES_ID);
    }

    // DataBaseCurdOperation dataBaseCurdOperation;
    Context mContext;

    /*  * Always return true, indicating that the
     * provider loaded correctly.
     */

    @Override
    public boolean onCreate() {
        // dataBaseCurdOperation = new DataBaseCurdOperation(getContext());
        Log.e("BASE_CONTENT_URI", BASE_CONTENT_URI.toString() + "   uriMatcher" + sUriMatcher);
        return true;
    }

    /* * Return no type for MIME type
     */
    @Override
    public String getType(Uri uri) {
        Log.e("uri", uri.toString());
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case ROUTE_ENTRIES:
                return CONTENT_TYPE;
            case ROUTE_ENTRIES_ID:
                return CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    /**
     * query() always returns no results
     **/
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.e(" uri ", "" + uri.toString() + "  selection  " + selection + "  selectionArgs  " + selectionArgs + "sortOrder" + sortOrder);
        return null;
    }

    /*  * insert() always returns null (no URI)
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        Log.e("uri", "" + uri.toString() + "content" + values);
        return null;
    }

    /*  * delete() always returns "no rows affected" (0)*/
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        Log.e("uri", "" + uri.toString() + "selection" + selection + "selectionArgs" + selectionArgs);
        return 0;
    }

    /* * update() always returns "no rows affected" (0)
     */
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        Log.e(" uri ", "" + uri.toString() + "  contentvalues  " + values + "  selection  " + selection + "  selectionArgs  " + selectionArgs);
        return 0;
    }
}
