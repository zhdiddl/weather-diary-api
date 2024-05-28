package zerobase.weather.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity(name = "memo") // memo 테이블에 맵핑할 엔티티로 지정
public class Memo {
    @Id // 테이블의 기본키 지정
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Spring Boot는 키 생성을 하지 않고 MySQL에게 맡김
    private int id;
    private String text;
}
