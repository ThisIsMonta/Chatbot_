package com.tynass.chatbot;

import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

//import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.ViewHolder> {

    private static final int VIEW_TYPE_USER_MESSSAGE = 0;
    private static final int VIEW_TYPE_BOT_MESSSAGE = 1;

    private ArrayList<Message> messages = new ArrayList<>();

    public MessageAdapter() {

    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        ViewHolder holder;
        switch (viewType){
            case Message.USER_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.user_message, parent, false);
                holder = new ViewHolder(view);
                return holder;
            case Message.BOT_MESSAGE:
                view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bot_message, parent, false);
                holder = new ViewHolder(view);
                return holder;
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        /*switch (holder.getItemViewType()){
            case Message.USER_MESSAGE:
                holder.image.setImageResource(R.drawable.user);//messages.get(position).getSender().getPhotoURL()
//                Picasso.get().load(messages.get(position).getSender().getPhotoURL()).into(holder.image);
            case Message.BOT_MESSAGE:
                holder.image.setImageResource(R.drawable.bot);
            default:
                holder.image.setImageResource(R.drawable.user);
        }*/
        if(holder.getItemViewType()==Message.USER_MESSAGE){
            holder.image.setImageResource(R.drawable.user);
        }else{
            holder.image.setImageResource(R.drawable.bot);
        }
        holder.message.setText(messages.get(position).message);
        holder.time.setText(messages.get(position).createdAt);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    @Override
    public int getItemViewType(int position) {
        Message msg = messages.get(position);

        if (msg.getSender().getUsername().equalsIgnoreCase("User")) {
            return Message.USER_MESSAGE;
        } else {
            return Message.BOT_MESSAGE;
        }
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
        notifyDataSetChanged();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private ImageView image;
        private TextView message;
        private TextView time;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            image = (ImageView) itemView.findViewById(R.id.bot_image);
            message = itemView.findViewById(R.id.bot_message);
            time = itemView.findViewById(R.id.bot_time);
        }
    }

}
