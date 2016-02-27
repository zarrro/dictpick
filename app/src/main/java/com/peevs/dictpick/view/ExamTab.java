package com.peevs.dictpick.view;

import android.app.Activity;
import android.app.Fragment;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
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

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.ExamDbHelper;
import com.peevs.dictpick.R;
import com.peevs.dictpick.TabFragmentHost;
import com.peevs.dictpick.TextToSpeechTask;
import com.peevs.dictpick.logic.QuestionFactory;
import com.peevs.dictpick.model.OpenQuestion;
import com.peevs.dictpick.model.Question;
import com.peevs.dictpick.model.TestQuestion;
import com.peevs.dictpick.model.TextEntry;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

public class ExamTab extends Fragment {

    class GenerateTestTask extends AsyncTask<Void, Void, Question> {

        private static final String TAG = "GenerateTestTask";
        private String srcText = null;

        /**
         * Loads all the translations, randomly select question word and wrong
         * testOptions as alternative.
         */
        @Override
        protected Question doInBackground(Void... n) {
            Log.d(TAG, "doInBackground started...");
            ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(parentActivity));
            QuestionFactory qf = new QuestionFactory(examDb, parentActivity.getForeignLanguage(),
                    parentActivity.getNativeLanguage());
            return qf.getTestQuestionByRating();
        }

        @Override
        protected void onPostExecute(Question result) {
            ExamTab.this.displayQuestion(result);
        }
    }

    /**
     * Insert new answerStat entry and return the new statistics as a result.
     */
    class UpdateStatsTask extends
            AsyncTask<Question, Void, ExamDbFacade.AnswerStats> {
        @Override
        protected ExamDbFacade.AnswerStats doInBackground(Question... params) {
            if (params.length != 1)
                throw new IllegalArgumentException();

            Question q = params[0];

            ExamDbFacade examDb = new ExamDbFacade(new ExamDbHelper(parentActivity));
            examDb.saveAnswer(q.getQuestion().getId(), q.getType(), q.getLastWrongAnswer());
            examDb.saveTranslation(currentQuestion.getQuestion(),
                    ExamDbContract.WordsTable.DEFAULT_BOOK_ID);

            // translation rating might have changes, so we notify the adapter for the wordsbook tab
            parentActivity.getContentResolver().notifyChange(
                    Uri.parse(ExamDbContract.WordsTable.CONTENT_URI), null);
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

        private class AnswerHandler implements View.OnClickListener {
            final Integer answerIndex;
            final AtomicBoolean answered;
            TestQuestion q;

            private AnswerHandler(Integer answerIndex, TestQuestion q, AtomicBoolean answered) {
                this.answerIndex = answerIndex;
                this.answered = answered;
                this.q = q;
            }

            @Override
            public void onClick(View v) {
                if (answered.compareAndSet(false, true)) {
                    if (q.checkAnswer(answerIndex)) {
                        v.setBackgroundColor(Color.GREEN);
                    } else {
                        v.setBackgroundColor(Color.RED);
                    }
                    updateStats(q);
                }
            }
        }

        AnswerHandler[] createAnswerHandlers(TestQuestion q) {
            AnswerHandler[] result = new AnswerHandler[q.getOptions().length];
            AtomicBoolean answeredFlag = new AtomicBoolean(false);
            for (int i = 0; i < result.length; ++i) {
                result[i] = new AnswerHandler(i, q, answeredFlag);
            }
            return result;
        }

        private TextView wordEntryToOptionView(TextEntry entry) {
            TextView result = new TextView(parentActivity);
            result.setText(entry.getText().getVal());
            result.setTextAppearance(parentActivity, R.style.TranslationTextStyle);
            return result;
        }

        public List<TextView> createAnswerViews(TestQuestion q) {

            TextView[] answerViews = new TextView[q.getOptions().length];
            AnswerHandler[] answerHandlers = createAnswerHandlers(q);

            for(int i = 0; i < answerViews.length; ++i) {
                answerViews[i] = wordEntryToOptionView(q.getOptions()[i]);
                answerViews[i].setOnClickListener(answerHandlers[i]);
            }

            return Arrays.asList(answerViews);
        }
    }

    private static final String TAG = "ExamTab";
    private Random rand = new Random(System.currentTimeMillis());
    private Question currentQuestion = null;
    private TestQuestion notificationQuestion = null;
    private TabFragmentHost parentActivity;
    private TextView questionView;
    private LinearLayout layout_answers;
    private TextView answerCountStat;
    private TextView answerSuccessRateStat;

    public ExamTab() {

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        parentActivity = (TabFragmentHost) activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_exam, container, false);
        attachButtonListeners(v);
        initViewMembers(v);
        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (notificationQuestion != null) {
            displayQuestion(notificationQuestion);
            notificationQuestion = null;
        }
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && currentQuestion == null && notificationQuestion == null) {
            new GenerateTestTask().execute();
        }
    }

    private void initViewMembers(View v) {
        questionView = (TextView) v.findViewById(R.id.question_text);
        layout_answers = (LinearLayout) v.findViewById(R.id.layout_answers);
        answerCountStat = (TextView) v.findViewById(R.id.ans_count_stat);
        answerSuccessRateStat = (TextView) v.findViewById(R.id.ans_success_rate_stat);
    }

    private void attachButtonListeners(View v) {
        ((Button) v.findViewById(R.id.btn_newtest)).setOnClickListener(
                new View.OnClickListener() {
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

    private void displayQuestion(Question question) {
        if (question == null)
            throw new IllegalArgumentException("question is null");

        Log.d(TAG, "displayQuestion invoked, question = " + question);

        // set the question word
        questionView.setText(
                question.getQuestionText().getVal());

        currentQuestion = question;

        if (parentActivity.getAutoSayQuestion()) {
            sayCurrentQuestion();
        }

        layout_answers.removeAllViews(); // clear the testOptions from previous question

        if(currentQuestion instanceof TestQuestion) {
            TestOptionViewsFactory tvFactory = new TestOptionViewsFactory();
            for (TextView optionView : tvFactory.createAnswerViews((TestQuestion) question)) {
                layout_answers.addView(optionView);
            }
        } else if (currentQuestion instanceof OpenQuestion) {
            layout_answers.addView(createOpenAnswer(question.getCorrectAnswer().
                    getText().getVal()));
        } else {
            throw new IllegalStateException("not supported question instance type");
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

    private void updateStats(Question q) {
        (new UpdateStatsTask()).execute(q);
    }

    public void sayCurrentQuestion() {
        //TODO: problem with playing BG, so play only foreignLang (EN)
        if (currentQuestion != null &&
                parentActivity.getForeignLanguage() == currentQuestion.getQuestionText().getLang()) {
            new TextToSpeechTask(currentQuestion.getQuestionText().getVal(),
                    currentQuestion.getQuestionText().getLang(),
                    parentActivity.getFilesDir()).execute();
        }
    }

    public TestQuestion getNotificationQuestion() {
        return notificationQuestion;
    }

    public void setNotificationQuestion(TestQuestion notificationQuestion) {
        this.notificationQuestion = notificationQuestion;
    }
}
