package stroom.query.api.v2;

import stroom.datasource.api.v2.AbstractField;
import stroom.datasource.api.v2.BooleanField;
import stroom.datasource.api.v2.DateField;
import stroom.datasource.api.v2.DocRefField;
import stroom.datasource.api.v2.DoubleField;
import stroom.datasource.api.v2.FloatField;
import stroom.datasource.api.v2.IdField;
import stroom.datasource.api.v2.IntegerField;
import stroom.datasource.api.v2.LongField;
import stroom.datasource.api.v2.TextField;
import stroom.docref.DocRef;
import stroom.query.api.v2.ExpressionOperator.Op;
import stroom.query.api.v2.ExpressionTerm.Condition;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ExpressionUtil {
    private ExpressionUtil() {
        // Utility class.
    }

    public static ExpressionOperator equals(final String field, final String value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final BooleanField field, final boolean value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final DateField field, final String value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final DocRefField field, final DocRef value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final IdField field, final long value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final IntegerField field, final int value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final LongField field, final long value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final FloatField field, final float value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final DoubleField field, final double value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final TextField field, final String value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static ExpressionOperator equals(final String field, final DocRef value) {
        return new ExpressionOperator.Builder(Op.AND)
                .addTerm(field, Condition.EQUALS, value)
                .build();
    }

    public static int termCount(final ExpressionOperator expressionOperator) {
        return terms(expressionOperator, null).size();
    }

    public static int termCount(final ExpressionOperator expressionOperator, final AbstractField field) {
        return terms(expressionOperator, Collections.singleton(field)).size();
    }

    public static int termCount(final ExpressionOperator expressionOperator, final Collection<AbstractField> fields) {
        return terms(expressionOperator, fields).size();
    }

    public static List<String> fields(final ExpressionOperator expressionOperator) {
        return terms(expressionOperator, null).stream().map(ExpressionTerm::getField).collect(Collectors.toList());
    }

    public static List<String> fields(final ExpressionOperator expressionOperator, final AbstractField field) {
        return terms(expressionOperator, Collections.singleton(field)).stream().map(ExpressionTerm::getField).collect(Collectors.toList());
    }

    public static List<String> fields(final ExpressionOperator expressionOperator, final Collection<AbstractField> fields) {
        return terms(expressionOperator, fields).stream().map(ExpressionTerm::getField).collect(Collectors.toList());
    }

    public static List<String> values(final ExpressionOperator expressionOperator) {
        return terms(expressionOperator, null).stream().map(ExpressionTerm::getValue).collect(Collectors.toList());
    }

    public static List<String> values(final ExpressionOperator expressionOperator, final AbstractField field) {
        return terms(expressionOperator, Collections.singleton(field)).stream().map(ExpressionTerm::getValue).collect(Collectors.toList());
    }

    public static List<String> values(final ExpressionOperator expressionOperator, final Collection<AbstractField> fields) {
        return terms(expressionOperator, fields).stream().map(ExpressionTerm::getValue).collect(Collectors.toList());
    }

    public static List<ExpressionTerm> terms(final ExpressionOperator expressionOperator, final Collection<AbstractField> fields) {
        final List<ExpressionTerm> terms = new ArrayList<>();
        addTerms(expressionOperator, fields, terms);
        return terms;
    }

    private static void addTerms(final ExpressionOperator expressionOperator, final Collection<AbstractField> fields, final List<ExpressionTerm> terms) {
        if (expressionOperator != null && expressionOperator.enabled() && !Op.NOT.equals(expressionOperator.op())) {
            for (final ExpressionItem item : expressionOperator.getChildren()) {
                if (item.enabled()) {
                    if (item instanceof ExpressionTerm) {
                        final ExpressionTerm expressionTerm = (ExpressionTerm) item;
                        if ((fields == null || fields.stream()
                                .anyMatch(field -> field.getName().equals(expressionTerm.getField()))) &&
                                expressionTerm.getValue() != null &&
                                expressionTerm.getValue().length() > 0) {
                            terms.add(expressionTerm);
                        }
                    } else if (item instanceof ExpressionOperator) {
                        addTerms((ExpressionOperator) item, fields, terms);
                    }
                }
            }
        }
    }

    public static ExpressionOperator copyOperator(final ExpressionOperator operator) {
        if (operator == null) {
            return null;
        }

        final ExpressionOperator.Builder builder = new ExpressionOperator.Builder(operator.getEnabled(), operator.getOp());
        operator.getChildren().forEach(item -> {
            if (item instanceof ExpressionOperator) {
                builder.addOperator(copyOperator((ExpressionOperator) item));

            } else if (item instanceof ExpressionTerm) {
                builder.addTerm(copyTerm((ExpressionTerm) item));
            }
        });
        return builder.build();
    }

    public static ExpressionTerm copyTerm(final ExpressionTerm term) {
        if (term == null) {
            return null;
        }

        final ExpressionTerm.Builder builder = new ExpressionTerm.Builder(term.getEnabled());
        builder.field(term.getField());
        builder.condition(term.getCondition());
        builder.value(term.getValue());
        builder.docRef(term.getDocRef());
        return builder.build();
    }
}
