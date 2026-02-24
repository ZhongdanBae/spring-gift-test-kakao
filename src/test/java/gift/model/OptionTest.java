package gift.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("Option 도메인")
class OptionTest {

    private Option createOption(int stock) {
        return new Option("테스트 옵션", stock, null);
    }

    @Nested
    @DisplayName("재고가 충분할 때")
    class WhenStockIsSufficient {

        @Test
        @DisplayName("수량만큼 재고가 줄어든다")
        void decreasesQuantityByGivenAmount() {
            // given
            Option option = createOption(10);

            // when
            option.decrease(3);

            // then
            assertThat(option.getQuantity()).isEqualTo(7);
        }

        @Test
        @DisplayName("재고와 동일한 수량이면 정확히 0이 된다")
        void decreasesToZeroWhenQuantityEqualsStock() {
            // given
            Option option = createOption(5);

            // when
            option.decrease(5);

            // then
            assertThat(option.getQuantity()).isEqualTo(0);
        }

        @Test
        @DisplayName("최소 단위(1)로 감소할 수 있다")
        void decreasesByOneWhenQuantityIsOne() {
            // given
            Option option = createOption(10);

            // when
            option.decrease(1);

            // then
            assertThat(option.getQuantity()).isEqualTo(9);
        }
    }

    @Nested
    @DisplayName("재고가 부족할 때")
    class WhenStockIsInsufficient {

        @Test
        @DisplayName("재고보다 많은 수량이면 IllegalStateException이 발생한다")
        void throwsExceptionWhenQuantityExceedsStock() {
            // given
            Option option = createOption(3);

            // when / then
            assertThatThrownBy(() -> option.decrease(4))
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("재고가 0이면 IllegalStateException이 발생한다")
        void throwsExceptionWhenStockIsZero() {
            // given
            Option option = createOption(0);

            // when / then
            assertThatThrownBy(() -> option.decrease(1))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("잘못된 입력")
    class WhenInputIsInvalid {

        @Test
        @DisplayName("수량 0이면 재고가 변하지 않는다")
        void doesNotChangeStockWhenQuantityIsZero() {
            // given
            Option option = createOption(10);

            // when
            option.decrease(0);

            // then
            assertThat(option.getQuantity()).isEqualTo(10);
        }

        @Test
        @DisplayName("음수 수량이면 IllegalArgumentException이 발생한다")
        void throwsExceptionWhenQuantityIsNegative() {
            // given
            Option option = createOption(10);

            // when / then
            assertThatThrownBy(() -> option.decrease(-5))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
