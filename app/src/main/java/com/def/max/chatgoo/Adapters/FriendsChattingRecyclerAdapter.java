package com.def.max.chatgoo.Adapters;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.crashlytics.android.Crashlytics;
import com.def.max.chatgoo.Models.FriendsMessageModel;
import com.def.max.chatgoo.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.content.Context.MODE_PRIVATE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;

public class FriendsChattingRecyclerAdapter extends RecyclerView.Adapter<FriendsChattingRecyclerAdapter.FriendsChattingViewHolder>
{
    private Context context;
    private ArrayList<FriendsMessageModel> friendsMessages;
    private String onlineUserId;

    public FriendsChattingRecyclerAdapter(Context context, ArrayList<FriendsMessageModel> friendsMessages, String onlineUserId)
    {
        this.context = context;
        this.friendsMessages = friendsMessages;
        this.onlineUserId = onlineUserId;
    }

    @NonNull
    @Override
    public FriendsChattingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.friends_chatting_layout_items,parent,false);

        return new FriendsChattingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final FriendsChattingViewHolder holder, int position)
    {
        final FriendsMessageModel model = friendsMessages.get(position);

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("Users");
        userRef.keepSynced(true);

        switch (model.getType())
        {
            case "text":

                if (model.getFrom().equals(onlineUserId))
                {
                    holder.thumb.setVisibility(GONE);

                    holder.receiverTextCardView.setVisibility(GONE);
                    holder.receiverImageCardView.setVisibility(GONE);

                    holder.senderImageCardView.setVisibility(GONE);

                    holder.senderTextCardView.setVisibility(VISIBLE);
                    holder.senderMessageText.setText(model.getMessage());
                    holder.senderMessageTextTime.setText(model.getTime());
                }
                else
                {
                    holder.thumb.setVisibility(VISIBLE);

                    userRef.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            Object objectThumb = dataSnapshot.child("user_profile_thumb_img").getValue();

                            if (objectThumb != null)
                            {
                                String userThumb = objectThumb.toString();

                                holder.setReceiverProfileImage(userThumb);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            Crashlytics.log(databaseError.getMessage());
                        }
                    });

                    holder.senderTextCardView.setVisibility(GONE);
                    holder.senderImageCardView.setVisibility(GONE);

                    holder.receiverImageCardView.setVisibility(GONE);

                    holder.receiverTextCardView.setVisibility(VISIBLE);
                    holder.receiverMessageText.setText(model.getMessage());
                    holder.receiverMessageTextTime.setText(model.getTime());
                }
                break;

            case "image":

                if (model.getFrom().equals(onlineUserId))
                {
                    holder.thumb.setVisibility(GONE);

                    holder.receiverTextCardView.setVisibility(GONE);
                    holder.receiverImageCardView.setVisibility(GONE);

                    holder.senderTextCardView.setVisibility(GONE);

                    holder.senderImageCardView.setVisibility(VISIBLE);
                    holder.senderMessageImageTime.setText(model.getTime());
                    holder.setSenderMessageImage(model.getStorage_uri());

                    holder.senderMessageImage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://"+ model.getStorage_uri()),"image/*");
                            context.startActivity(intent);
                        }
                    });
                }
                else
                {
                    holder.thumb.setVisibility(VISIBLE);

                    userRef.addValueEventListener(new ValueEventListener()
                    {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                        {
                            Object objectThumb = dataSnapshot.child("user_profile_thumb_img").getValue();

                            if (objectThumb != null)
                            {
                                String userThumb = objectThumb.toString();

                                holder.setReceiverProfileImage(userThumb);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError)
                        {
                            Crashlytics.log(databaseError.getMessage());
                        }
                    });

                    holder.senderTextCardView.setVisibility(GONE);
                    holder.senderImageCardView.setVisibility(GONE);

                    holder.receiverTextCardView.setVisibility(GONE);

                    holder.receiverImageCardView.setVisibility(VISIBLE);
                    holder.receiverMessageImageTime.setText(model.getTime());

                    final String imageResult = getChatLocalStorageRef(context,model.getKey());

                    if (imageResult != null)
                    {
                        holder.setReceiverMessageImage(imageResult);
                    }
                    else
                    {
                        StorageReference downloadRef = FirebaseStorage.getInstance().getReferenceFromUrl(model.getMessage());

                        final File localFile = new File(Environment.getExternalStorageDirectory() + "/ChatGoo/", "Images");

                        boolean created = false;

                        if (!localFile.exists())
                        {
                            created = localFile.mkdirs();
                        }

                        if (created)
                        {
                            final File downloadFile = new File(localFile, "IMG_" + System.currentTimeMillis() + ".jpg");

                            downloadRef.getFile(downloadFile).addOnCompleteListener(new OnCompleteListener<FileDownloadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<FileDownloadTask.TaskSnapshot> task)
                                {
                                    if (task.isSuccessful())
                                    {
                                        Uri file = Uri.fromFile(downloadFile);

                                        setChatLocalStorageRef(context,model.getKey(),file.toString());

                                        final String result = getChatLocalStorageRef(context,model.getKey());

                                        holder.setReceiverMessageImage(result);

                                        holder.receiverMessageImage.setOnClickListener(new View.OnClickListener()
                                        {
                                            @Override
                                            public void onClick(View v)
                                            {
                                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                                intent.setDataAndType(Uri.parse("file://"+ result),"image/*");
                                                context.startActivity(intent);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    }

                    holder.receiverMessageImage.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            intent.setDataAndType(Uri.parse("file://"+ imageResult),"image/*");
                            context.startActivity(intent);
                        }
                    });
                }

                break;
        }
    }

    @Override
    public int getItemCount() {
        return friendsMessages.size();
    }

    class FriendsChattingViewHolder extends RecyclerView.ViewHolder
    {
        private CircleImageView thumb;
        private AppCompatTextView receiverMessageText,receiverMessageTextTime,senderMessageText,senderMessageTextTime,senderMessageImageTime,receiverMessageImageTime;
        private AppCompatImageView senderMessageImage,receiverMessageImage;
        private CardView senderTextCardView,senderImageCardView,receiverTextCardView,receiverImageCardView;

        private View view;

        FriendsChattingViewHolder(View itemView)
        {
            super(itemView);

            view = itemView;

            thumb = view.findViewById(R.id.message_receiver_circle_image_view);
            senderMessageText = view.findViewById(R.id.sender_message_text);
            senderMessageTextTime = view.findViewById(R.id.sender_message_text_time);
            senderMessageImage = view.findViewById(R.id.sender_message_image);
            senderMessageImageTime = view.findViewById(R.id.sender_message_image_time);
            receiverMessageText = view.findViewById(R.id.receiver_message_text);
            receiverMessageTextTime = view.findViewById(R.id.receiver_message_text_time);
            receiverMessageImage = view.findViewById(R.id.receiver_message_image);
            receiverMessageImageTime = view.findViewById(R.id.receiver_message_image_time);
            senderTextCardView = view.findViewById(R.id.sender_text_card_view);
            senderImageCardView = view.findViewById(R.id.sender_image_card_view);
            receiverTextCardView = view.findViewById(R.id.receiver_text_card_view);
            receiverImageCardView = view.findViewById(R.id.receiver_image_card_view);
        }

        private void setReceiverProfileImage(final String thumbImg)
        {
            final CircleImageView thumb = view.findViewById(R.id.message_receiver_circle_image_view);

            Picasso.with(context).load(thumbImg).networkPolicy(NetworkPolicy.OFFLINE).into(thumb, new Callback()
            {
                @Override
                public void onSuccess()
                {

                }

                @Override
                public void onError()
                {
                    Picasso.with(context).load(thumbImg).placeholder(R.drawable.user_icon).into(thumb);
                }
            });
        }

        private void setSenderMessageImage(final String thumbImg)
        {
            final AppCompatImageView thumb = view.findViewById(R.id.sender_message_image);

            Picasso.with(context).load(thumbImg).networkPolicy(NetworkPolicy.OFFLINE).into(thumb, new Callback()
            {
                @Override
                public void onSuccess()
                {

                }

                @Override
                public void onError()
                {
                    Picasso.with(context).load(thumbImg).placeholder(R.drawable.image_bg).into(thumb);
                }
            });
        }

        private void setReceiverMessageImage(final String thumbImg)
        {
            final AppCompatImageView thumb = view.findViewById(R.id.receiver_message_image);

            Picasso.with(context).load(thumbImg).networkPolicy(NetworkPolicy.OFFLINE).into(thumb, new Callback()
            {
                @Override
                public void onSuccess()
                {

                }

                @Override
                public void onError()
                {
                    Picasso.with(context).load(thumbImg).placeholder(R.drawable.image_bg).into(thumb);
                }
            });
        }
    }

    private String getTodayDate()
    {
        DateFormat todayDate = new SimpleDateFormat("d MMM yyyy", Locale.US);

        return todayDate.format(Calendar.getInstance().getTime());
    }

    private String getChatLocalStorageRef(Context context, String key)
    {
        String get = null;
        String result;

        SharedPreferences postPreference = context.getSharedPreferences("chats_storage_ref",MODE_PRIVATE);

        if (postPreference != null)
        {
            get = postPreference.getString(key,null);
        }

        if (get == null)
        {
            result = null;
        }
        else
        {
            result = get;
        }

        return result;
    }

    private void setChatLocalStorageRef(Context context,String key, String path)
    {
        SharedPreferences preferences = context.getSharedPreferences("chats_storage_ref",MODE_PRIVATE);

        if (preferences != null)
        {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key,path);
            editor.apply();
        }
    }
}
