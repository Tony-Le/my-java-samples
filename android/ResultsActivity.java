package ca.eecsyorku.letony28.scrollcompare;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class ResultsActivity extends Activity implements View.OnClickListener {

    private TextView targetsCompletedTextView, inputModeTextView, completionTimeTextView, totalOvershootTime;
    private Button setupButton;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        targetsCompletedTextView = (TextView)findViewById(R.id.textView1);
        inputModeTextView = (TextView)findViewById(R.id.textView2);
        completionTimeTextView = (TextView)findViewById(R.id.textView3);
        totalOvershootTime = (TextView)findViewById(R.id.textView4);

        setupButton = (Button)findViewById(R.id.setup);

        Bundle b = getIntent().getExtras();
        targetsCompletedTextView.setText("Targets Completed: " + b.getInt("targetsCompleted"));
        inputModeTextView.setText("Input Mode : " + b.getString("inputMode"));
        completionTimeTextView.setText("Completion time: " + b.getLong("completionTime") + " s");
        totalOvershootTime.setText("OvershootingTime: " +b.getLong("overshootingTime", 0) + " s");

        setupButton.setOnClickListener(this);
    }

    public void onClick(View v)
    {
        if (v.equals(setupButton))
        {
            Intent i = new Intent(getApplicationContext(), SetupActivity.class);
            startActivity(i);
            this.finish();
        }
    }

}
