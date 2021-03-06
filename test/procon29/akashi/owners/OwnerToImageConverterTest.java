package procon29.akashi.owners;

import org.junit.Test;
import org.junit.runner.RunWith;
import procon29.akashi.side.Side;

import static org.junit.Assert.assertEquals;

@RunWith(JavaFxJUnit4ClassRunner.class)
public class OwnerToImageConverterTest {
    OwnerToImageConverter ownerToImageConverter;

    @Test
    public void SamePointerReturnTest() {
        ownerToImageConverter = new OwnerToImageConverter(Side.None);
        assertEquals(ownerToImageConverter.convert(Owner.None), ownerToImageConverter.convert(Owner.None));

        assertEquals(ownerToImageConverter.convert(Owner.Friend), ownerToImageConverter.convert(Owner.Friend));

        assertEquals(ownerToImageConverter.convert(Owner.Enemy), ownerToImageConverter.convert(Owner.Enemy));
    }
}