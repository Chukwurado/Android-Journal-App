package ui;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.view.LayoutInflaterCompat;
import androidx.core.widget.TextViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.self.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import model.Journal;

public class JournalRecyclerAdapter extends RecyclerView.Adapter<JournalRecyclerAdapter.ViewHolder> {
    private Context context;
    private List<Journal> journalList;

    public JournalRecyclerAdapter(Context context, List<Journal> journalList) {
        this.context = context;
        this.journalList = journalList;
    }

    @NonNull
    @Override
    public JournalRecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                                        //or context
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.journal_row, parent, false);

        return new ViewHolder(view, context);
    }

    //this is where you set the values of widgets created in Viewholder
    @Override
    public void onBindViewHolder(@NonNull JournalRecyclerAdapter.ViewHolder holder, int position) {
        Journal journal = journalList.get(position);
        String imageUrl = journal.getImageUrl();

        holder.title.setText(journal.getTitle());
        holder.thoughts.setText(journal.getThought());

        //Set the time of when journals are posted - 3 hours ago
        String timeAgo = (String) DateUtils.getRelativeTimeSpanString(journal.getTimeAdded().getSeconds() * 1000);


        holder.dataAdded.setText(timeAgo);
        holder.name.setText(journal.getUsername());

        Picasso.get()
                .load(imageUrl)
                .placeholder(R.drawable.image_one)
                .fit()
                .into(holder.image);


    }

    @Override
    public int getItemCount() {
        return journalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        //This is where all the widgets are created
        public TextView title, thoughts, dataAdded, name;

        public ImageView image;
        public ImageButton shareButton;
        String userId;
        String username;

        //add context in case you want to start a new activity
        public ViewHolder(@NonNull View itemView, Context ctx) {
            super(itemView);
            title = itemView.findViewById(R.id.journal_title_list);
            thoughts = itemView.findViewById(R.id.journal_thought_list);
            dataAdded = itemView.findViewById(R.id.journal_timestamp_list);
            image = itemView.findViewById(R.id.journal_image_list);
            name = itemView.findViewById(R.id.journal_row_username);
            shareButton = itemView.findViewById(R.id.journal_row_share_button);

            shareButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //context.startActivity();
                }
            });


        }
    }
}
