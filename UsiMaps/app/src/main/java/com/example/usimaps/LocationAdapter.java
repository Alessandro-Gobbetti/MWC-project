package com.example.usimaps;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.annotation.SuppressLint;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * A RecyclerView adapter for displaying a list of locations.
 *
 * This adapter binds a list of locations to a RecyclerView, allowing the user to
 * select a location from the list. It includes functionality for handling click events
 * on individual items and updating the dataset dynamically.
 */
public class LocationAdapter extends RecyclerView.Adapter<LocationAdapter.ViewHolder> {

    private List<String> locations;
    private OnItemClickListener onItemClickListener;
    private Context context;

    /**
     * Constructs a new LocationAdapter.
     *
     * @param context The context in which the adapter is used.
     * @param locations The initial list of locations to be displayed.
     */
    public LocationAdapter(Context context, List<String> locations) {
        this.context = context;
        this.locations = locations;
    }

    /**
     * Interface for handling click events on RecyclerView items.
     */
    public interface OnItemClickListener {
        /**
         * Called when a location item is clicked.
         *
         * @param location The location that was clicked.
         */
        void onItemClick(String location);
    }

    /**
     * Sets a listener for handling click events on location items.
     *
     * @param listener The listener to be set.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Updates the list of locations displayed by the adapter.
     *
     * @param newLocations The new list of locations to display.
     */
    @SuppressLint("NotifyDataSetChanged")
    public void updateData(List<String> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }

    /**
     * Inflates the layout for a single RecyclerView item.
     *
     * @param parent The parent ViewGroup into which the new view will be added.
     * @param viewType The view type of the new view (not used in this implementation).
     * @return A new ViewHolder for the item view.
     */
    @Override
    public LocationAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(
                android.R.layout.simple_list_item_1, parent, false);
        return new LocationAdapter.ViewHolder(view);
    }

    /**
     * Binds data to a RecyclerView item view.
     *
     * @param holder The ViewHolder for the item.
     * @param position The position of the item in the dataset.
     */
    @Override
    public void onBindViewHolder(LocationAdapter.ViewHolder holder, int position) {
        String location = locations.get(position);
        holder.textView.setText(location);
        holder.itemView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(location);
            }
        });
    }

    /**
     * Returns the total number of items in the dataset.
     *
     * @return The number of items in the dataset.
     */
    @Override
    public int getItemCount() {
        return locations.size();
    }

    /**
     * A ViewHolder for a RecyclerView item.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        /**
         * Constructs a new ViewHolder for the item view.
         *
         * @param itemView The item view.
         */
        public ViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(android.R.id.text1);
        }
    }
}
