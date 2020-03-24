package option_2;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class Customer {

    private Long id;
    private String name;

    public static List<Customer> getData(){
        return new ArrayList<>();
    }
}
