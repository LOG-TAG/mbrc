package com.kelsos.mbrc.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v7.widget.PopupMenu;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import com.kelsos.mbrc.R;
import com.kelsos.mbrc.data.GenreEntry;

import java.util.ArrayList;

public class GenreEntryAdapter extends RecyclerView.Adapter<GenreEntryAdapter.ViewHolder> {
    private ArrayList<GenreEntry> mData;
    private Typeface robotoRegular;
    private MenuItemSelectedListener mListener;

    public GenreEntryAdapter(Context context, ArrayList<GenreEntry> objects) {
        this.mData = objects;
        robotoRegular = Typeface.createFromAsset(context.getAssets(), "fonts/roboto_regular.ttf");
    }

    public void setMenuItemSelectedListener(MenuItemSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Called when RecyclerView needs a new {@link ViewHolder} of the given type to represent
     * an item.
     * <p/>
     * This new ViewHolder should be constructed with a new View that can represent the items
     * of the given type. You can either create a new View manually or inflate it from an XML
     * layout file.
     * <p/>
     * The new ViewHolder will be used to display items of the adapter using
     * {@link #onBindViewHolder(ViewHolder, int)}. Since it will be re-used to display different
     * items in the data set, it is a good idea to cache references to sub views of the View to
     * avoid unnecessary {@link View#findViewById(int)} calls.
     *
     * @param parent   The ViewGroup into which the new View will be added after it is bound to
     *                 an adapter position.
     * @param viewType The view type of the new View.
     * @return A new ViewHolder that holds a View of the given view type.
     * @see #getItemViewType(int)
     * @see #onBindViewHolder(ViewHolder, int)
     */
    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ui_list_single, parent, false);
        return new ViewHolder(view, robotoRegular);
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * should update the contents of the {@link ViewHolder#itemView} to reflect the item at
     * the given position.
     * <p/>
     * Note that unlike {@link ListView}, RecyclerView will not call this
     * method again if the position of the item changes in the data set unless the item itself
     * is invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside this
     * method and should not keep a copy of it. If you need the position of an item later on
     * (e.g. in a click listener), use {@link ViewHolder#getPosition()} which will have the
     * updated position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        final GenreEntry entry = mData.get(position);
        holder.title.setText(entry.getName());

        holder.indicator.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), v);
                popupMenu.inflate(R.menu.popup_genre);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override public boolean onMenuItemClick(MenuItem menuItem) {
                        if (mListener != null) {
                            mListener.onMenuItemSelected(menuItem, entry);
                            return true;
                        }
                        return false;
                    }
                });
                popupMenu.show();
            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mListener != null) {
                    mListener.onItemClicked(entry);
                }
            }
        });
    }

    /**
     * Returns the total number of items in the data set hold by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public interface MenuItemSelectedListener {
        void onMenuItemSelected(MenuItem menuItem, GenreEntry entry);

        void onItemClicked(GenreEntry genre);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        LinearLayout indicator;

        public ViewHolder(View itemView, Typeface typeface) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.line_one);
            title.setTypeface(typeface);
            indicator = (LinearLayout) itemView.findViewById(R.id.ui_item_context_indicator);
        }
    }
}
