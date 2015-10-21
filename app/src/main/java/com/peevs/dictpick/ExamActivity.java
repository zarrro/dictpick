package com.peevs.dictpick;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;


/**
 * Created by zarrro on 16.8.2015 г..
 */
public class ExamActivity extends BaseActivity {

    private static final String TAG = "ExamActivity";
    private Random rand = new Random(System.currentTimeMillis());

    class GenerateTestTask extends AsyncTask<Void, Void, TestQuestion> {

        private static final String TAG = "GenerateTestTask";
        private String srcText = null;

        /**
         * Loads all the translations, randomly select question word and wrong testOptions as alternative.
         */
        @Override
        protected TestQuestion doInBackground(Void... n) {
            Log.d(TAG, "doInBackground started...");
            ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(ExamActivity.this));
            return examDb.getRandomTestQuestion(srcLang, targetLang,
                    TestQuestion.WRONG_OPTIONS_COUNT);
        }

        @Override
        protected void onPostExecute(TestQuestion result) {
            ExamActivity.this.displayTestQuestion(result);
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

            ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(ExamActivity.this));
            examDb.saveAnswer(params[0]);
            return examDb.queryAnswerStats();
        }

        @Override
        protected void onPostExecute(ExamDbFacade.AnswerStats answerStats) {
            ExamActivity.this.updateAnswerStatsView(answerStats.getCount(),
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
            int questionWordId;

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

            Correct(TextView[] optionViews, int questionWordId) {
                this.questionWordId = questionWordId;
                this.optionViews = optionViews;
            }

            @Override
            public void onClick(View v) {
                markCorrectOption(v);
                disableAnswersClick();
                ExamActivity.this.updateStats(questionWordId, -1);
            }
        }

        private class Wrong extends OptionMarker {
            private int wrongAnswerWordId;

            Wrong(TextView[] optionViews, int questionWordId, int wrongAnswerWordId,
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
                ExamActivity.this.updateStats(questionWordId, wrongAnswerWordId);
            }
        }

        private TextView wordEntryToTextView(TestQuestion.WordEntry entry) {
            TextView result = new TextView(ExamActivity.this);
            result.setText(entry.getText());
            result.setTextSize(TypedValue.COMPLEX_UNIT_PT, TEST_OPTION_TEXT_SIZE);
            return result;
        }

        private void setCorrectOptionView(TextView[] optionViews, int correctOptionIndex,
                                          TestQuestion q) {
            TextView correctAnswer = wordEntryToTextView(q.getCorrectOptionWordEntry());
            correctAnswer.setOnClickListener(new Correct(optionViews,
                    q.getCorrectOptionWordEntry().getId()));
            optionViews[correctOptionIndex] = correctAnswer;
        }

        private void setWrongOptionView(TextView[] optionViews, int wrongOptionIndex,
                                        TestQuestion q) {
            TextView result = wordEntryToTextView(q.getOptions()[wrongOptionIndex]);
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

    void updateStats(int questionWordId, int wrongAnswerId) {
        ExamDbFacade.AnswerStatsEntry statsIn = new ExamDbFacade.AnswerStatsEntry();
        statsIn.setQuestionWordId(questionWordId);
        statsIn.setWrongAnswerWordId(wrongAnswerId);
        statsIn.setTimestamp(new Date());
        (new UpdateStatsTask()).execute(statsIn);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.exam);

        Intent intent = getIntent();

        TestQuestion testQuestion = intent.getParcelableExtra(
                NotificationPublisher.QUESTION_FROM_NOTIFICATION);
        if (testQuestion != null) {
            Log.i(TAG, String.format(
                    "ExamActivity started from notification intent %s, with testQuestion:%n %s",
                    intent.toString(), testQuestion.toString()));
            displayTestQuestion(testQuestion);
        } else {
            // activity is started from within the DictPick app - start with a new question
            generateTestQuestion(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void generateTestQuestion(View v) {
        new GenerateTestTask().execute();
    }

    public void displayTestQuestion(TestQuestion testQuestion) {
        Log.d(TAG, "displayTestQuestion invoked, testQuestion = " + testQuestion);

        if (testQuestion == null) {
            Toast.makeText(ExamActivity.this, "Couldn't retrieve test question...",
                    Toast.LENGTH_LONG).show();
            return;
        }

        // set the question word
        ((TextView) findViewById(R.id.question_text)).setText(testQuestion.getQuestion().getText());
        questionWordId = testQuestion.getQuestion().getId();

        LinearLayout answersLayout = (LinearLayout) findViewById(R.id.layout_answers);

        // clear the testOptions from previous question
        answersLayout.removeAllViews();

        TestOptionViewsFactory tvFactory = new TestOptionViewsFactory();

        for (TextView optionView : tvFactory.createAnswerViews(testQuestion)) {
            answersLayout.addView(optionView);
        }
    }


    public void updateAnswerStatsView(long count, float successRate) {

        // set the question word
        ((TextView) findViewById(R.id.ans_count_stat)).setText(
                String.format("Answers Count: %d", count));
        ((TextView) findViewById(R.id.ans_success_rate_stat)).setText(
                String.format("Success Rate: %.2f", successRate * 100) + "%");
    }
}
