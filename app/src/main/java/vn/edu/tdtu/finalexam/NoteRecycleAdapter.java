package vn.edu.tdtu.finalexam;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.MediaController;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class NoteRecycleAdapter extends RecyclerView.Adapter<NoteRecycleAdapter.ViewHolder> implements Filterable {

    private Context context;
    private List<NoteRecycle> notes;
    private List<NoteRecycle> oldNotes;
    private OnItemClicked onClick;
    private DatabaseReference user = FirebaseDatabase.getInstance("https://final-exam-27d93-default-rtdb.firebaseio.com/").getReference();

    public interface OnItemClicked {
        void onItemClick(int position);
    }

    public NoteRecycleAdapter(Context context, List<NoteRecycle> notes) {
        this.context = context;
        this.notes = notes;
        this.oldNotes= notes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.note_item,parent,false);

        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        NoteRecycle note = this.notes.get(position);
        holder.tvTitle.setText(note.getTitle());
        holder.tvDescript.setText(note.getDescrip());
        holder.tvCreateTime.setText("Thời gian xóa: "+note.getAddingTime());
//        holder.itemView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onClick.onItemClick(holder.getAdapterPosition());
//            }
//        });
        if(!note.getImgPath().isEmpty())
        {
            holder.tvImage.setImageBitmap(BitmapFactory.decodeFile(note.getImgPath()));
            holder.tvImage.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.tvImage.setVisibility(View.GONE);
        }

        if(!note.getVideoPath().isEmpty())
        {
            Uri video  = Uri.parse(note.getVideoPath());
            MediaController controller = new MediaController(context);
            controller.setAnchorView(holder.tvVideo);
            controller.setMediaPlayer(holder.tvVideo);
            holder.tvVideo.setMediaController(controller);
            holder.tvVideo.setVideoURI(video);
            holder.tvVideo.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.tvVideo.setVisibility(View.GONE);
        }

        SharedPreferences sp =context.getSharedPreferences("gridLayoutManager", Context.MODE_PRIVATE);
        String span = sp.getString("span", "");

        if(span.equals("3"))
        {
            holder.tvTitle.setGravity(Gravity.CENTER);
            holder.tvDescript.setGravity(Gravity.CENTER);
            holder.tvCreateTime.setGravity(Gravity.CENTER);
        }
        else
        {
            holder.tvTitle.setGravity(Gravity.LEFT);
            holder.tvDescript.setGravity(Gravity.LEFT);
            holder.tvCreateTime.setGravity(Gravity.RIGHT);
        }
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp =context.getSharedPreferences("User", 0);
                String phone = sp.getString("phone", "");
                PopupMenu menu = new PopupMenu(context,v);
                menu.getMenu().add("Khôi phục");
                menu.getMenu().add("Xóa");
                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        if(menuItem.getTitle().equals("Xóa"))
                        {
                           user.child("users").child(phone)
                                    .child("trash").child(note.getId())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Toast.makeText(context, "Xóa ghi chú thành công", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                            notes.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            notifyItemRangeChanged(holder.getAdapterPosition(),notes.size() - holder.getAdapterPosition());
                        }
                        if(menuItem.getTitle().equals("Khôi phục"))
                        {
                            user.child("users").child(phone)
                                    .child("trash").child(note.getId())
                                    .removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            Note noteRecover = new Note(note.getId(), note.getTitle(), note.getDescrip(), note.getCreatedTime()
                                                    , note.getImgPath(),note.getVideoPath() );

                                            Toast.makeText(context, "Khôi phục ghi chú thành công", Toast.LENGTH_SHORT).show();
                                            user.child("users").child(phone)
                                                    .child("notes").child(note.getId()).setValue(noteRecover);
                                        }
                                    });
                            notes.remove(holder.getAdapterPosition());
                            notifyItemRemoved(holder.getAdapterPosition());
                            notifyItemRangeChanged(holder.getAdapterPosition(),notes.size() - holder.getAdapterPosition());
                        }
                        return true;
                    }
                });
                menu.show();
            }
        });
    }

    public void setOnClick(OnItemClicked onClick){
        this.onClick=onClick;
    }

    public void setNotesList(List<NoteRecycle> notes) {
        this.notes = notes;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return this.notes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        VideoView tvVideo;
        ImageView tvImage;
        TextView tvTitle,tvDescript,tvCreateTime;
        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescript = itemView.findViewById(R.id.tvDescript);
            tvCreateTime = itemView.findViewById(R.id.tvCreateTime);
            tvImage = itemView.findViewById(R.id.tvImage);
            tvVideo = itemView.findViewById(R.id.tvVideo);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String strSearch = charSequence.toString();
                if(strSearch.isEmpty())
                {
                    notes = oldNotes;
                }
                else
                {
                    List<NoteRecycle> note = new ArrayList<>();
                    for (NoteRecycle keyword: oldNotes)
                    {
                        if(keyword.getTitle().toLowerCase().contains(strSearch.toLowerCase())
                                || keyword.getDescrip().toLowerCase().contains(strSearch.toLowerCase()))
                        {
                            note.add(keyword);
                        }
                    }

                    notes = note;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = notes;

                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                notes = (List<NoteRecycle>) filterResults.values;
                notifyDataSetChanged();
            }
        };
    }

}
