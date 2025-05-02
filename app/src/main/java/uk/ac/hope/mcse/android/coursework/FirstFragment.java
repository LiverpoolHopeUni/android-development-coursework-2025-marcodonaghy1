package uk.ac.hope.mcse.android.coursework;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import uk.ac.hope.mcse.android.coursework.databinding.FragmentFirstBinding;

/**
 *
 */
public class FirstFragment extends Fragment {

    private static final String PREF_NAME = "EventPrefs";
    private static final String KEY_EVENT_LIST = "event_list";
    private static final String KEY_SELECTED_EVENT = "selected_event";
    private static final String EVENT_SEPARATOR = "|||"; // Unique separator for events
    
    private FragmentFirstBinding binding;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPreferences = requireActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentFirstBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        loadEventList();
        setupClickListeners();
        animateViewsIn();
    }

    private void animateViewsIn() {
        binding.textviewFirst.setAlpha(0f);
        binding.buttonClearEvents.setAlpha(0f);
        binding.buttonRespondEvent.setAlpha(0f);
        binding.fab.setAlpha(0f);

        binding.textviewFirst.animate()
                .alpha(1f)
                .setDuration(500)
                .start();

        binding.buttonClearEvents.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(200)
                .start();

        binding.buttonRespondEvent.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(400)
                .start();

        binding.fab.animate()
                .alpha(1f)
                .setDuration(500)
                .setStartDelay(600)
                .start();
    }

    private void setupClickListeners() {
        binding.buttonClearEvents.setOnClickListener(v -> {
            animateButtonClick(v);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.remove(KEY_EVENT_LIST);
            editor.apply();
            binding.textviewFirst.setText(R.string.no_events_yet);
            Toast.makeText(requireContext(), "All events cleared", Toast.LENGTH_SHORT).show();
        });

        binding.buttonRespondEvent.setOnClickListener(v -> {
            animateButtonClick(v);
            NavHostFragment.findNavController(FirstFragment.this)
                    .navigate(R.id.action_FirstFragment_to_RespondFragment);
        });

        binding.fab.setOnClickListener(v -> 
            NavHostFragment.findNavController(this)
                .navigate(R.id.action_FirstFragment_to_SecondFragment));
    }

    private void animateButtonClick(View view) {
        view.animate()
                .scaleX(0.95f)
                .scaleY(0.95f)
                .setDuration(100)
                .withEndAction(() -> view.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                        .start())
                .start();
    }

    private void loadEventList() {
        executorService.execute(() -> {
            final String eventList = sharedPreferences.getString(KEY_EVENT_LIST, "");
            
            mainHandler.post(() -> {
                if (eventList.isEmpty()) {
                    binding.textviewFirst.setText(R.string.no_events_yet);
                    binding.textviewFirst.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                } else {
                    String[] events = eventList.split(EVENT_SEPARATOR);
                    StringBuilder formattedEvents = new StringBuilder();
                    
                    for (String event : events) {
                        if (!event.trim().isEmpty()) {
                            String[] parts = event.split("\n");
                            if (parts.length >= 3) {
                                formattedEvents.append(parts[0])
                                        .append("\n")
                                        .append(parts[1])
                                        .append(" at ")
                                        .append(parts[2])
                                        .append("\n\n");
                            }
                        }
                    }
                    
                    binding.textviewFirst.setText(formattedEvents.toString().trim());
                    binding.textviewFirst.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
    }

}