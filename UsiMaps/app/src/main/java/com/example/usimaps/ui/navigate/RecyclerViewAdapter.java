package com.example.usimaps.ui.navigate;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.usimaps.R;
import com.example.usimaps.map.Vertex;
import com.example.usimaps.map.VertexType;

import java.util.ArrayList;
import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder>{

    private List<Vertex> path;
    private List<Fragment> fragmentList = new ArrayList();
    private List<String> instructions = new ArrayList();


    public RecyclerViewAdapter(List<Vertex> path, List<String> instructions) {
        this.path = path;
        this.instructions = instructions;
        // log
        System.out.println("...............path: " + path);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView iconView;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            textView = (TextView) view.findViewById(R.id.instruction_text);
            iconView = (ImageView) view.findViewById(R.id.instruction_icon);

        }

        public TextView getTextView() {
            return textView;
        }

        public ImageView getIconView() {
            return iconView;
        }
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.fragment_instruction_item, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.getTextView().setText(instructions.get(position));

        // set the icon
        if (position == 0) {
            viewHolder.getIconView().setImageResource(R.drawable.map_pin);
        } else if (position == path.size() - 1) {
            viewHolder.getIconView().setImageResource(R.drawable.goal);
        } else {
            VertexType type = path.get(position).getType();
            if (type == VertexType.STAIR || instructions.get(position).contains("stairs"))
                viewHolder.getIconView().setImageResource(R.drawable.stairs);
            else if (instructions.get(position).contains("left")) {
                viewHolder.getIconView().setImageResource(R.drawable.ic_turn_left);
            } else if (instructions.get(position).contains("right")) {
                viewHolder.getIconView().setImageResource(R.drawable.ic_turn_right);
            } else {
                viewHolder.getIconView().setImageResource(R.drawable.ic_arrow_up);
            }
        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return path.size();
    }

    // Method to update the list of instructions
    public void updatePath(List<Vertex> newPath, List<String> newInstructions) {
        path = newPath;
        instructions = newInstructions;
        notifyDataSetChanged();
    }

    public List<Vertex> getPath() {
        return path;
    }

}
