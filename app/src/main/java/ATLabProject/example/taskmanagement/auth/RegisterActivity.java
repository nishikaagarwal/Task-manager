package ATLabProject.example.taskmanagement.auth;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import ATLabProject.example.taskmanagement.R;
import ATLabProject.example.taskmanagement.SignInDone;

public class RegisterActivity extends AppCompatActivity {

    ImageView gotoGoogle;


    GoogleApiClient mGoogleApiClient;
    private static final int RC_SIGN_IN = 9001;
    private DatabaseReference UsersReference;
    private FirebaseFirestore firestore;

    Map<String, String> ordermap = new HashMap<>();
    private FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        gotoGoogle=findViewById(R.id.gotoGoogleBtn);

        ImageView emailSignin = findViewById(R.id.emailsignin);
        ImageView phonenosignin = findViewById(R.id.phonenosignin);

        firestore = FirebaseFirestore.getInstance();

        // Intent to Sign in with Phone No Activity
        phonenosignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(RegisterActivity.this, PhoneNoSignin.class);
                startActivity(i);
            }
        });
        // Intent to Email sign in Activity
        emailSignin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, SigninActivity.class);
                startActivity(intent);

            }
        });


        gotoGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                        .requestIdToken("710523477665-t3snpd89faf73nkki3nvpr9jtceer3ld.apps.googleusercontent.com")
                        .requestEmail()
                        .build();


                mGoogleApiClient = new GoogleApiClient.Builder(RegisterActivity.this)
                        .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                        .build();


                Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
            startActivityForResult(signInIntent, RC_SIGN_IN);


            }
        });
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                //TO USE
                String personName = acct.getDisplayName();
                String personEmail = acct.getEmail();
                String personId = acct.getId();
                Uri personPhoto = acct.getPhotoUrl();

                String st = String.valueOf(personPhoto);



                firebaseAuthWithGoogle(acct,personName,personEmail,st);


            } else {
                Toast.makeText(RegisterActivity.this,"There was a trouble signing in-Please try again",Toast.LENGTH_SHORT).show();;
            }
        }
    }


    public void storeUserOnFireBase(String userName,String email,String phone,String passwd,String UID,String st){
        final FirebaseDatabase database=FirebaseDatabase.getInstance();
        UsersReference = database.getReference().child("Users");


        UsersReference.child(UID).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (!(dataSnapshot.child("name").exists()))
                {
                    HashMap userMap=new HashMap();
                    userMap.put("name",userName);
                    userMap.put("email",email);
                    userMap.put("Phone_Number",phone);
                    if(!userMap.containsKey("user_image")) userMap.put("user_image",st);
                    //userMap.put("Password",passwd);

                    UsersReference.child(UID).updateChildren(userMap).addOnCompleteListener(new OnCompleteListener() {
                        @Override
                        public void onComplete(@NonNull Task task) {
                            if(task.isSuccessful())
                            {

                                Toast.makeText(RegisterActivity.this, "Successfully sign in", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(),task.getException().toString(),Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    });


                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });




    }


    private void firebaseAuthWithGoogle(GoogleSignInAccount acct,String name,String email,String st) {
        mAuth = FirebaseAuth.getInstance();
        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                        }else{
                            mAuth = FirebaseAuth.getInstance();
                            String currentUserID = mAuth.getCurrentUser().getUid().toString();
                            storeUserOnFireBase(name,email,"+91 - ","",currentUserID,st);
                            Toast.makeText(RegisterActivity.this, "Authentication pass.",
                                    Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, SignInDone.class);
                            startActivity(intent);
                            RegisterActivity.this.finish();
                        }
                    }
                });
    }




}