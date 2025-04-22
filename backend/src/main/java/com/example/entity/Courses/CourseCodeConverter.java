package com.example.entity.Courses;

import com.example.exception.GeneralExc;

import jakarta.persistence.Embeddable;

@Embeddable
public class CourseCodeConverter {
    public int code_to_id(String to_convert){
        if (to_convert != null){
            String[] parts = to_convert.split("-");
            String prefix = parts[0]; // 'cs'
            String suffix = parts[1]; // '319'
            int prefix_number = prefix_to_int(prefix); 
            int suffix_number = Integer.parseInt(suffix); // 319
            String id = prefix_number + "" + suffix_number; // 'c' + 's' + 319
            return Integer.parseInt(id); // cs-319 -> 319319
        }
        return 0; // default value if course_code is null
    }

    private int prefix_to_int(String prefix){
        String to_return = "" ;
        for(int i = 0; i < prefix.length(); i++){
            int c = prefix.charAt(i) - 'a' + 1; // 'c' -> 3, 's' -> 19
            if (c < 0 || c > 26) {
                throw new IllegalArgumentException("Invalid prefix character: " + prefix.charAt(i));
            }
            to_return += c; // 'c' + 's' -> 319
        }
        return Integer.parseInt(to_return) ; 
    }

    public int code_to_id_sec(String to_convert){
        if (to_convert != null){
            to_convert = to_convert.toUpperCase() ;
            String[] parts = to_convert.split("-");
            String prefix = parts[0]; 
            String suffix = parts[1]; 
            int prefix_number = prefix_to_int_sec(prefix); 
            int suffix_number = Integer.parseInt(suffix); 
            String id = prefix_number + "" + suffix_number; 
            return Integer.parseInt(id); 
        }
        return 0; // default value if course_code is null
    }

    private int prefix_to_int_sec(String prefix){
        String to_return = "" ;
        for(int i = 0; i < prefix.length(); i++){
            int c = prefix.charAt(i) ;
            if (c >= 48 && c <= 57)
                to_return += c - 48 ;
            else if (c >= 'A' && c <= 'Z') {
                to_return += c - 'A' + 1;
            }
            else 
                throw new GeneralExc("Invalid prefix character: " + prefix.charAt(i));
        }
        return Integer.parseInt(to_return) ; 
    }
}
