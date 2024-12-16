package com.example.usimaps;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.map.Vertex;
import com.example.usimaps.map.VertexType;

import java.util.ArrayList;
import java.util.List;

import kotlin.Pair;
import kotlin.Triple;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder>{

    private List<Triple<String, String, String>> history = new ArrayList<>();
    private OnItemClickListener onClickListener;

    public interface OnItemClickListener {
        void onItemClick(Triple<String, String, String> item);
    }

    public HistoryAdapter(List<Triple<String, String, String>> history, OnItemClickListener onClickListener) {
        this.history = history;
        this.onClickListener = onClickListener;
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewDate;
        private final TextView textViewStart;
        private final TextView textViewGoal;


        public ViewHolder(View view, OnItemClickListener onClickListener) {
            super(view);
            // Define click listener for the ViewHolder's View
            textViewDate = (TextView) view.findViewById(R.id.date_text);
            textViewStart = (TextView) view.findViewById(R.id.start_text);
            textViewGoal = (TextView) view.findViewById(R.id.goal_text);

            view.setOnClickListener(v -> {
                if (onClickListener != null) {
                    System.out.println("Clicked History Item Adapter");
                    int position = getBindingAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        System.out.println("Clicked History Item Adapter!!!");
                        onClickListener.onItemClick((Triple<String, String, String>) v.getTag());
                    }
                }
            });
        }

        public void bind(Triple<String, String, String> item) {
            textViewDate.setText(item.getFirst());
            textViewStart.setText(item.getSecond());
            textViewGoal.setText(item.getThird());
            itemView.setTag(item);
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_history_item, viewGroup, false);
        return new ViewHolder(view, onClickListener);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.bind(history.get(position));
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return history.size();
    }

}
