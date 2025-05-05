package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RespondFragment extends Fragment {
    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_RESPONSE = "event_response";
    private static final String KEY_SELECTED_EVENT = "selected_event";
    private static final int ANIMATION_DURATION = 500;
    private static final int BUTTON_ANIMATION_DURATION = 100;
    
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    
    private SharedPreferences sharedPreferences;
    private TextInputEditText editTextResponse;
    private MaterialButton buttonSubmitResponse;
    private MaterialButton buttonBack;
    private View eventCard;
    private View textViewTitle;
    private TextInputLayout responseInputLayout;
    private TextView textViewEventDetails;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_respond, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupClickListeners();
        loadEventDetails();
        loadSavedResponse();
        animateViewsIn();
    }

    private void initializeViews(View view) {
        editTextResponse = view.findViewById(R.id.editTextResponse);
        buttonSubmitResponse = view.findViewById(R.id.buttonSubmitResponse);
        buttonBack = view.findViewById(R.id.buttonBack);
        eventCard = view.findViewById(R.id.eventCard);
        textViewTitle = view.findViewById(R.id.textViewTitle);
        responseInputLayout = view.findViewById(R.id.responseInputLayout);
        textViewEventDetails = view.findViewById(R.id.textViewEventDetails);
    }

    private void loadEventDetails() {
        executorService.execute(() -> {
            String eventDetails = sharedPreferences.getString(KEY_SELECTED_EVENT, "");
            if (!eventDetails.isEmpty()) {
                mainHandler.post(() -> {
                    textViewEventDetails.setText(eventDetails);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.remove(KEY_SELECTED_EVENT);
                    editor.apply();
                });
            } else {
                mainHandler.post(() -> {
                    textViewEventDetails.setText(R.string.no_event_selected);
                    buttonSubmitResponse.setEnabled(false);
                });
            }
        });
    }

    private void setupClickListeners() {
        buttonSubmitResponse.setOnClickListener(this::handleSubmitClick);
        buttonBack.setOnClickListener(this::handleBackClick);
    }

    private void handleSubmitClick(View v) {
        animateButtonClick(v, () -> {
            String response = editTextResponse.getText().toString().trim();
            if (response.isEmpty()) {
                showToast("Please enter a response");
                return;
            }
            saveResponse(response);
            showToast("Response submitted!");
            navigateToFirstFragment();
        });
    }

    private void handleBackClick(View v) {
        animateButtonClick(v, this::navigateToFirstFragment);
    }

    private void animateButtonClick(View v, Runnable action) {
        v.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(BUTTON_ANIMATION_DURATION)
                .withEndAction(() -> {
                    v.animate()
                            .scaleX(1f)
                            .scaleY(1f)
                            .setDuration(BUTTON_ANIMATION_DURATION)
                            .start();
                    action.run();
                })
                .start();
    }

    private void saveResponse(String response) {
        executorService.execute(() -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_RESPONSE, response);
            editor.apply();
        });
    }

    private void loadSavedResponse() {
        executorService.execute(() -> {
            String savedResponse = sharedPreferences.getString(KEY_RESPONSE, "");
            if (!savedResponse.isEmpty()) {
                mainHandler.post(() -> editTextResponse.setText(savedResponse));
            }
        });
    }

    private void showToast(String message) {
        mainHandler.post(() -> Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show());
    }

    private void navigateToFirstFragment() {
        mainHandler.post(() -> 
            NavHostFragment.findNavController(RespondFragment.this)
                    .navigate(R.id.action_RespondFragment_to_FirstFragment)
        );
    }

    private void animateViewsIn() {
        animateViewFadeIn(textViewTitle, 100);
        animateViewSlideUp(eventCard, 200);
        animateViewFadeIn(responseInputLayout, 300);
        animateViewFadeIn(buttonSubmitResponse, 400);
        animateViewFadeIn(buttonBack, 400);
    }

    private void animateViewFadeIn(View view, int delay) {
        view.setAlpha(0f);
        view.animate()
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(delay)
                .start();
    }

    private void animateViewSlideUp(View view, int delay) {
        view.setTranslationY(50f);
        view.setAlpha(0f);
        view.animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(ANIMATION_DURATION)
                .setStartDelay(delay)
                .start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }
} 