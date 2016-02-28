package com.peevs.dictpick.logic;

import com.peevs.dictpick.ExamDbContract;
import com.peevs.dictpick.ExamDbFacade;
import com.peevs.dictpick.Language;
import com.peevs.dictpick.model.OpenQuestion;
import com.peevs.dictpick.model.Question;
import com.peevs.dictpick.model.TestQuestion;
import com.peevs.dictpick.model.TextEntry;
import com.peevs.dictpick.model.TranslationEntry;

import java.util.List;
import java.util.Random;

/**
 * Created by zarrro on 16.2.2016 г..
 */
public class QuestionFactory {

    final Random rand;

    private ExamDbFacade examDb;
    private Language fLang;
    private Language nLang;

    public QuestionFactory(ExamDbFacade examDb, Language foreignLang, Language nativeLang) {
        this.examDb = examDb;
        this.rand = new Random(System.currentTimeMillis());
        this.fLang = foreignLang;
        this.nLang = nativeLang;
    }

    public Question getTestQuestionByRating() {

        List<TranslationEntry> translations = examDb.queryTranslations(fLang, nLang,
                ExamDbContract.WordsTable.DEFAULT_BOOK_ID, ExamDbContract.WordsTable.RATING + " ASC",
                1);

        if(translations.isEmpty()) return null;

        TranslationEntry translation = translations.get(0);

        Question result;
        switch (selectTypeByRating(translation.getRating())) {
            case TEST:
                // we call get randomTestQueston TestQuestion instance with initialized alternatives
                // options, then we just reinitialize the translation for the question
                TestQuestion t = examDb.getRandomTestQuestion(fLang, nLang,
                        TestQuestion.WRONG_OPTIONS_COUNT, ExamDbContract.WordsTable.DEFAULT_BOOK_ID);

                t.setQuestion(translation);
                result = t;
                break;
            case OPEN:
                result = new OpenQuestion(translation);
                result.setInverse(rand.nextBoolean());
                break;
            default:
                throw new IllegalStateException("unhandled test question type");
        }
        return result;
    }

    private Question.Type selectTypeByRating(int rating) {
        // bigger ratings has bigger chances to get OPEN question
        int r = rand.nextInt(Question.MAX_RATING);
        if(r <= rating) {
            return Question.Type.OPEN;
        } else {
            return Question.Type.TEST;
        }
    }
}
