package com.tynass.chatbot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.android.AIConfiguration;
import ai.api.android.AIService;
import ai.api.model.AIError;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;

public class MainActivity extends AppCompatActivity implements ai.api.AIListener {

    /*Static Variables*/
    public static int HOW_MANY_MESSAGES=0;

    /* Firebase References */
    FirebaseAuth fAuth;
    FirebaseDatabase fDatabase;
    DatabaseReference dbRef;

    /* Project Variables */
    EditText message;
    TextView indicator;
    ImageButton send_btn;
    ImageButton mic_btn;
    RecyclerView messages_recycler;
    DatabaseReference photoUrlRef;
    String photoURL;
    ImageView imageView;
    String botMessage;
    String time = new SimpleDateFormat("HH:mm").format(Calendar.getInstance().getTime());
    final ArrayList<Message> messages = new ArrayList<>();
    final MessageAdapter messageAdapter = new MessageAdapter();
    String messageKey;
    final String status = "<small>\uD83D\uDFE2 Online</small>";
    /* DialogFlow */
    AIService aiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        fAuth = FirebaseAuth.getInstance();
        fDatabase = FirebaseDatabase.getInstance();
        dbRef = fDatabase.getReference().child("Chatbot");
        message = findViewById(R.id.message_input);
        send_btn = findViewById(R.id.send_btn);
        mic_btn = findViewById(R.id.btn_micro);
        indicator = findViewById(R.id.indicatorBot);
//        photoUrlRef = fDatabase.getReference().child(fAuth.getCurrentUser().getUid()).child("user");


        if (fAuth.getCurrentUser() == null) {
            Toast.makeText(this, "You are not logged in !!!", Toast.LENGTH_SHORT).show();
            Intent intentLoginActivity = new Intent(this, LoginActivity.class);
            startActivity(intentLoginActivity);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.RECORD_AUDIO
            }, 101);
        }


        final AIConfiguration config = new AIConfiguration("239001d3094444b390e30d3e3ea6832b",
                AIConfiguration.SupportedLanguages.English,
                AIConfiguration.RecognitionEngine.System);

        aiService = AIService.getService(this, config);
        final AIDataService aiDataService = new AIDataService(config);
        final AIRequest aiRequest = new AIRequest();
        aiService.setListener(this);

        messages_recycler = findViewById(R.id.messages_recycler);
        messages_recycler.setLayoutManager(new LinearLayoutManager(this));

        /*photoUrlRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds : snapshot.getChildren()) {
                    photoURL = ds.child("photoURL").getValue().toString();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });*/

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                messages.clear();
                for(DataSnapshot ds : snapshot.getChildren()) {
                    if(ds.getKey().equals(fAuth.getCurrentUser().getUid())){
                        for(DataSnapshot messages_firebase : ds.getChildren()) {
                            System.out.println(messages_firebase.child("message").getValue());
                            messages.add(new UserMessage((String) messages_firebase.child("message").getValue(),new User((String) messages_firebase.child("sender").child("username").getValue(),photoURL),time));
                            HOW_MANY_MESSAGES+=1;
                        }
                    }else {
                        Toast.makeText(MainActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                    messageAdapter.setMessages(messages);
                    messages_recycler.setAdapter(messageAdapter);
                    messages_recycler.scrollToPosition(HOW_MANY_MESSAGES);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


//        getSupportActionBar().setSubtitle(Html.fromHtml(status));

        message.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                messages_recycler.scrollToPosition(HOW_MANY_MESSAGES);
            }
        });

        send_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.fadein);
                send_btn.startAnimation(animation);
                final String user_message = message.getText().toString();
                if (message.length() <= 0) {
                    Toast.makeText(MainActivity.this, "You have to type something !", Toast.LENGTH_LONG).show();
                } else {
                    messageKey = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                    addUserMessages(messageKey+"0",user_message);
                    aiRequest.setQuery(user_message);
                    new AsyncTask<AIRequest, Void, AIResponse>() {
                        @Override
                        protected AIResponse doInBackground(AIRequest... requests) {
                            final AIRequest request = requests[0];
                            try {
                                final AIResponse response = aiDataService.request(aiRequest);
                                return response;
                            } catch (AIServiceException e) {
                            }
                            return null;
                        }
                        @Override
                        protected void onPostExecute(AIResponse aiResponse) {
                            if (aiResponse != null || !aiResponse.getResult().getFulfillment().getSpeech().isEmpty()) {
                                messageKey = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                                addBotMessages(messageKey+"1",aiResponse.getResult().getFulfillment().getSpeech());
                            }
                            else {
                                messageKey = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
                                addBotMessages(messageKey+"1","Sorry, i didn't get that");
                            }
                        }
                    }.execute(aiRequest);
                }
            }
        });
        mic_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Animation animation = AnimationUtils.loadAnimation(MainActivity.this,R.anim.fadein);
                mic_btn.startAnimation(animation);
                aiService.startListening();
                getSupportActionBar().setSubtitle(Html.fromHtml("<small> Bot is listening...</small>"));
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.log_out:
                Toast.makeText(this, "Logging out", Toast.LENGTH_SHORT).show();
                fAuth.signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                return true;
            case R.id.quit:
                Toast.makeText(this, "Quitting", Toast.LENGTH_SHORT).show();
                finish();
                return true;
            default:
                Toast.makeText(this, "Nothing", Toast.LENGTH_SHORT).show();
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResult(AIResponse response) {
        Result result = response.getResult();
        messageKey = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
        addUserMessages(new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime())+"0",result.getResolvedQuery());
        if(result.getFulfillment().getSpeech()!=null){
            messageKey = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
            addBotMessages(messageKey+"1",result.getFulfillment().getSpeech());
        }else{
            Toast.makeText(this, "Respond error", Toast.LENGTH_SHORT).show();
            messageKey = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(Calendar.getInstance().getTime());
            addBotMessages(messageKey+"1","Sorry, i didn't get that");
        }
    }

    @Override
    public void onError(AIError error) {
        Toast.makeText(this, "Listening error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onAudioLevel(float level) {

    }

    @Override
    public void onListeningStarted() {
        Toast.makeText(this, "Listening started", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListeningCanceled() {
        Toast.makeText(this, "Listening canceled", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onListeningFinished() {
        Toast.makeText(this, "Listening finished", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    public void addUserMessages(String messageKey,String msgUser){
        HOW_MANY_MESSAGES+=1;
        User user = new User("User", photoURL);
        UserMessage userMsg = new UserMessage(msgUser,user, time);
        dbRef.child(fAuth.getUid()).child(messageKey).setValue(userMsg);
        message.setText("");
        messages_recycler.smoothScrollToPosition(HOW_MANY_MESSAGES);
        indicator.setText("Bot is typing...");
    }

    public void addBotMessages(final String messageKey,final String msgBot){
        HOW_MANY_MESSAGES+=1;
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                User bot = new User("Bot", photoURL);
                BotMessage botMsg = new BotMessage(msgBot,bot, time);
                indicator.setText("");
                dbRef.child(fAuth.getUid()).child(messageKey).setValue(botMsg);
            }
        },1000);
        messages_recycler.smoothScrollToPosition(HOW_MANY_MESSAGES);
    }
}