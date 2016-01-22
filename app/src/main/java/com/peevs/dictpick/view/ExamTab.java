package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.R;
import com.peevs.dictpick.TabFragmentHost;
import com.peevs.dictpick.TextToSpeechTask;
import com.peevs.dictpick.model.TestQuestion;
import com.peevs.dictpick.model.TextEntry;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ExamTab extends Fragment {

    class GenerateTestTask extends AsyncTask<Void, Void, TestQuestion> {

        private static final String TAG = "GenerateTestTask";
        private String srcText = null;

        /**
         * Loads all the translations, randomly select question word and wrong testOptions as alternative.
         */
        @Override
        protected TestQuestion doInBackground(Void... n) {
            Log.d(TAG, "doInBackground started...");
            ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(parentActivity));
            return examDb.getRandomTestQuestion(parentActivity.getForeignLanguage(),
                    parentActivity.getNativeLanguage(),
                    TestQuestion.WRONG_OPTIONS_COUNT,
                    ExamDbContract.WordsTable.DEFAULT_BOOK_ID);
        }

        @Override
        protected void onPostExecute(TestQuestion result) {
            ExamTab.this.displayTestQuestion(result);
        }
    }

    /**
     * Insert new answerStat entry and return the new statistics as a result.
     */
    class UpdateStatsTask extends
            AsyncTask<ExamDbFacade.AnswerStatsEntry, Void, ExamDbFacade.AnswerStats> {
        @Override
        protected ExamDbFacade.AnswerStats doInBackground(ExamDbFacade.AnswerStatsEntry... params) {
            if (params.length != 1)
                throw new IllegalArgumentException();

            ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(parentActivity));
            examDb.saveAnswer(params[0]);
            return examDb.queryAnswerStats();
        }

        @Override
        protected void onPostExecute(ExamDbFacade.AnswerStats answerStats) {
            updateAnswerStatsView(answerStats.getCount(),
                    answerStats.getSuccessRate());
        }
    }


    /**
     * Factory for the test question answer views.
     */
    class TestOptionViewsFactory {

        private static final int TEST_OPTION_TEXT_SIZE = 16;

        private abstract class OptionMarker implements View.OnClickListener {
            TextView[] optionViews;
            int correctOptionIndex;
            long questionWordId;

            void disableAnswersClick() {
                for (TextView tv : optionViews) {
                    tv.setOnClickListener(null);
                    tv.setClickable(false);
                }
            }

            void markCorrectOption(View v) {
                v.setBackgroundColor(Color.GREEN);
            }

            void markWrongOption(View v) {
                v.setBackgroundColor(Color.RED);
            }
        }

        private class Correct extends OptionMarker {

            Correct(TextView[] optionViews, long questionWordId) {
                this.questionWordId = questionWordId;
                this.optionViews = optionViews;
            }

            @Override
            public void onClick(View v) {
                markCorrectOption(v);
                disableAnswersClick();
                ExamTab.this.updateStats(questionWordId, -1);
            }
        }

        private class Wrong extends OptionMarker {
            private long wrongAnswerWordId;

            Wrong(TextView[] optionViews, long questionWordId, long wrongAnswerWordId,
                  int correctOptionIndex) {
                this.questionWordId = questionWordId;
                this.optionViews = optionViews;
                this.correctOptionIndex = correctOptionIndex;
                this.wrongAnswerWordId = wrongAnswerWordId;
            }

            @Override
            public void onClick(View v) {
                markWrongOption(v);
                markCorrectOption(optionViews[correctOptionIndex]);
                disableAnswersClick();
                updateStats(questionWordId, wrongAnswerWordId);
            }
        }

        private TextView wordEntryToOptionView(TextEntry entry) {
            TextView result = new TextView(parentActivity);
            result.setText(entry.getText().getVal());
            result.setTextAppearance(parentActivity, R.style.TranslationTextStyle);
            return result;
        }

        private void setCorrectOptionView(TextView[] optionViews, int correctOptionIndex,
                                          TestQuestion q) {
            TextView correctAnswer = wordEntryToOptionView(q.getCorrectOptionWordEntry());
            correctAnswer.setOnClickListener(new Correct(optionViews,
                    q.getCorrectOptionWordEntry().getId()));
            optionViews[correctOptionIndex] = correctAnswer;
        }

        private void setWrongOptionView(TextView[] optionViews, int wrongOptionIndex,
                                        TestQuestion q) {
            TextView result = wordEntryToOptionView(q.getOptions()[wrongOptionIndex]);
            result.setOnClickListener(new Wrong(optionViews, q.getCorrectOptionWordEntry().getId(),
                    q.getOptions()[wrongOptionIndex].getId(),
                    q.getCorrectOptionIndex()));
            optionViews[wrongOptionIndex] = result;
        }

        public List<TextView> createAnswerViews(TestQuestion q) {

            TextView[] optionViews = new TextView[q.getOptions().length];

            setCorrectOptionView(optionViews, q.getCorrectOptionIndex(), q);

            for (int i = 0; i < q.getCorrectOptionIndex(); ++i) {
                setWrongOptionView(optionViews, i, q);
            }
            for (int i = q.getCorrectOptionIndex() + 1; i < optionViews.length; ++i) {
                setWrongOptionView(optionViews, i, q);
            }

            return Arrays.asList(optionViews);
        }
    }

    private static final String TAG = "ExamTab";
    private Random rand = new Random(System.currentTimeMillis());
    private TestQuestion currentQuestion = null;

    private TestQuestion testQuestion = null;
    private TabFragmentHost parentActivity;

    private TextView questionView;
    private LinearLayout layout_answers;
    private TextView answerCountStat;
    private TextView answerSuccessRateStat;

    public ExamTab() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (TabFragmentHost) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (testQuestion != null) {
            Log.i(TAG, String.format(
                    "ExamTab started with testQuestion:%n %s", testQuestion));
            displayTestQuestion(testQuestion);
        } else {
            // activity is started from within the DictPick app - start with a new question
            new GenerateTestTask().execute();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exam, container, false);
        initViewMembers(v);
        attachButtonListeners(v);
        return v;
    }

    private void initViewMembers(View v) {
        questionView = (TextView) v.findViewById(R.id.question_text);
        layout_answers = (LinearLayout) v.findViewById(R.id.layout_answers);
        answerCountStat = (TextView) v.findViewById(R.id.ans_count_stat);
        answerSuccessRateStat = (TextView) v.findViewById(R.id.ans_success_rate_stat);
    }

    private void attachButtonListeners(View v) {
        ((Button) v.findViewById(R.id.btn_newtest)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new GenerateTestTask().execute();
            }
        });
        ((Button) v.findViewById(R.id.btn_listen_exam)).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        sayCurrentQuestion();
                    }
                });
    }

    private void displayTestQuestion(TestQuestion testQuestion) {
        Log.d(TAG, "displayTestQuestion invoked, testQuestion = " + testQuestion);

        if (testQuestion == null) {
            Toast.makeText(parentActivity, "Couldn't retrieve test question...",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // set the question word
        questionView.setText(
                testQuestion.getQuestion().getVal());

        this.currentQuestion = testQuestion;
        if (parentActivity.getAutoSayQuestion()) {
            sayCurrentQuestion();
        }

        layout_answers.removeAllViews(); // clear the testOptions from previous question

        boolean test = Math.random() > 0.5;
        if (test) {
            TestOptionViewsFactory tvFactory = new TestOptionViewsFactory();
            for (TextView optionView : tvFactory.createAnswerViews(testQuestion)) {
                layout_answers.addView(optionView);
            }
        } else {
            layout_answers.addView(createOpenAnswer(testQuestion.getCorrectOptionWordEntry().
                    getText().getVal()));
        }
    }

    private ViewGroup createOpenAnswer(final String correctAnswer) {
        LinearLayout openQuestionLayout = new LinearLayout(parentActivity);
        openQuestionLayout.setOrientation(LinearLayout.VERTICAL);

        RelativeLayout layout = new RelativeLayout(parentActivity);
        openQuestionLayout.addView(layout);
        EditText answerEdit = new EditText(parentActivity);
        Button check = new Button(parentActivity);
        check.setText("Check");
        layout.addView(answerEdit);
        layout.addView(check);

        answerEdit.setHint("Type answer...");
        answerEdit.requestFocus();


        ((RelativeLayout.LayoutParams) answerEdit.getLayoutParams()).addRule(
                RelativeLayout.ALIGN_PARENT_LEFT);
        ((RelativeLayout.LayoutParams) check.getLayoutParams()).addRule(
                RelativeLayout.ALIGN_PARENT_RIGHT);

        class AnswerCheckListener implements View.OnClickListener {
            EditText et;
            String correct;
            ViewGroup layout;
            ViewGroup outter;

            @Override
            public void onClick(View v) {
                String actual = et.getText().toString().trim().toLowerCase();
                if (correct.toLowerCase().equals(actual)) {
                    et.setTextColor(Color.GREEN);
                } else {
                    et.setTextColor(Color.RED);
                    et.setPaintFlags(
                            et.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    TextView correctAnswer = new TextView(parentActivity);
                    outter.addView(correctAnswer, 0);

                    correctAnswer.setText(correct);
                    correctAnswer.setTextAppearance(parentActivity,
                            R.style.Base_TextAppearance_AppCompat_Large);
                    correctAnswer.setTextColor(Color.GREEN);

                    v.setEnabled(false);
                }
            }
        }

        AnswerCheckListener ac = new AnswerCheckListener();
        ac.et = answerEdit;
        ac.correct = correctAnswer;
        ac.layout = layout;
        ac.outter = openQuestionLayout;
        check.setOnClickListener(ac);
        return openQuestionLayout;
    }

    private void updateAnswerStatsView(long count, float successRate) {
        // set the question word
        answerCountStat.setText(String.format("Answers Count: %d", count));
        answerSuccessRateStat.setText(String.format("Success Rate: %.2f", successRate * 100) + "%");
    }

    private void updateStats(long questionWordId, long wrongAnswerId) {
        ExamDbFacade.AnswerStatsEntry statsIn = new ExamDbFacade.AnswerStatsEntry();
        statsIn.setQuestionWordId(questionWordId);
        statsIn.setWrongAnswerWordId(wrongAnswerId);
        statsIn.setTimestamp(new Date());
        (new UpdateStatsTask()).execute(statsIn);
    }

    public void sayCurrentQuestion() {
        //TODO: problem with playing BG, so play only foreignLang (EN)
        if (currentQuestion != null &&
                parentActivity.getForeignLanguage() == currentQuestion.getQuestion().getLang()) {
            new TextToSpeechTask(currentQuestion.getQuestion().getVal(),
                    currentQuestion.getQuestion().getLang(),
                    parentActivity.getFilesDir()).execute();
        }
    }
}
