package com.example.android.lipstickinventory;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.android.lipstickinventory.Networking.Search;

import java.util.List;

/**
 * {@link SearchAdapter} knows how to create a list layout for {@link Search} object
 */

public class SearchAdapter extends ArrayAdapter<Search> {

    /**
     *Constructs new {@link SearchAdapter}
     *
     * @param context of the app
     * @param searches list of searches
     */
    public SearchAdapter(@NonNull Context context, List<Search> searches) {
        super(context, 0, searches);
    }

    /**
     * Returns a list item view that displays search results
     *
     * @param position of search item
     * @param convertView
     * @param parent
     * @return list view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //Check if there is an existing view
        View listItemView = convertView;
        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.search_list_item, parent, false);
        }

        //Find search for current position
        Search currentSearchItem = getItem(position);

        //Find and display for title
        TextView titleView = (TextView) listItemView.findViewById(R.id.title_text);
        titleView.setText(currentSearchItem.getTitle());

        //Find and display for snippet
        TextView snippetView = (TextView) listItemView.findViewById(R.id.snippet_text);
        snippetView.setText(currentSearchItem.getSnippet());

        return listItemView;
    }
}