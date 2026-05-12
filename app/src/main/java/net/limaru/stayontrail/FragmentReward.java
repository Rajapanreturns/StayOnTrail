package net.limaru.stayontrail;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class FragmentReward extends Fragment {

  private static final int TIER_2 = 50;
  private static final int TIER_3 = 200;

  @Nullable
  @Override
  public View onCreateView(
      @NonNull LayoutInflater inflater,
      @Nullable ViewGroup container,
      @Nullable Bundle savedInstanceState
  ) {
    return inflater.inflate(R.layout.fragment_streak_view, container, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);

    TextView scoreText = view.findViewById(R.id.score);
    ImageView forestImage = view.findViewById(R.id.forest_image);

    SqlHelper db = new SqlHelper(requireContext());

    //db.setStreakTasks(0);

    int tasks = db.getStreakTasks();
    db.close();

    scoreText.setText(String.valueOf(tasks));
    updateForestUI(forestImage, tasks);
  }

  private void updateForestUI(ImageView forestView, int tasks) {
    int imageRes;
    float currentScale;

    if (tasks >= 500) {
      imageRes = R.drawable.forest3;
      currentScale = 1.0f;
    }
    else if (tasks >= 200) {
      imageRes = R.drawable.forest3;
      float progress = (float) (tasks - 200) / (500 - TIER_3);
      currentScale = 0.5f + (progress * (1.0f - 0.5f));
    }
    else if (tasks >= 50) {
      imageRes = R.drawable.forest2;
      float progress = (float) (tasks - 50) / (TIER_3 - TIER_2);
      currentScale = 0.75f + (progress * (1.5f - 0.75f));
    }
    else {
      imageRes = R.drawable.forest1;
      float progress = (float) tasks / TIER_2;
      currentScale = 0.6f + (progress * (1.2f - 0.6f));
    }

    forestView.setImageResource(imageRes);

    forestView.setAlpha(tasks == 0 ? 0.4f : 1.0f);

    forestView.animate()
        .scaleX(currentScale)
        .scaleY(currentScale)
        .setDuration(500)
        .start();
  }
}
