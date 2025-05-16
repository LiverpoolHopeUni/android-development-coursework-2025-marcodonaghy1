package uk.ac.hope.mcse.android.coursework;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import uk.ac.hope.mcse.android.coursework.databinding.DialogPrivacyPolicyBinding;

public class PrivacyPolicyFragment extends Fragment {

    private DialogPrivacyPolicyBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
        binding = DialogPrivacyPolicyBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set the privacy policy text
        binding.textPrivacyPolicy.setText(getString(R.string.privacy_policy));

        // Set up the close button
        binding.toolbar.setNavigationOnClickListener(v -> {
            requireActivity().onBackPressed();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 