package zerobase.weather.repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

import javax.sql.DataSource;
import java.util.List;
import java.util.Optional;


@Repository
public class JdbcMemoRepository {
    private final JdbcTemplate jdbcTemplate;

    // 생성자
    // DataSource: 데이터베이스 연결 풀을 관리하는 객체
    @Autowired
    public JdbcMemoRepository(DataSource datasource) {
        jdbcTemplate = new JdbcTemplate(datasource);
    }

    // DB에 Memo 객체를 저장하는 메소드
    public Memo save(Memo memo) {
        String sql = "insert into memo values(?,?)"; // 물음표는 update 메소드에서 채워진다
        jdbcTemplate.update(sql, memo.getId(), memo.getText());
        return memo;
    }

    // DB에 저장한 Memo 객체를 조회해서 List로 반환하는 메소드
    public List<Memo> findAll() {
        String sql = "select * from memo"; // 조회는 query 메소드로 가능
        return jdbcTemplate.query(sql, memoRowMapper()); // ResultSet으로 반환 받은 객체를 맵퍼 메소드를 이용해서 가져온다
    }

    // DB에 저장한 Memo 객체를 id로 조회하는 메소드 (조회할 값이 없어서 Optional.empty()가 실행되는 경우 어떻게 처리하는지 확인해보기)
    public Optional<Memo> findById(int id) {
        String sql = "select * from memo where id = ?";
        return jdbcTemplate.query(sql, memoRowMapper(), id).stream().findFirst(); // 스트림의 첫 번째 요소를 가져온다
    }

    // DB에서 가져온 rs를 Memo 객체로 맵핑하는 메소드
    // RowMapper: ResultSet(DB에서 가져온 값)을
    // ResultSet {id = 1, text = 'this is memo!'} => 스프링 부트의 memo 클래스 형식으로 맵핑
    private RowMapper<Memo> memoRowMapper() {
        return (rs, rowNum) -> new Memo(
                rs.getInt("id"),
                rs.getString("text") // @AllArgumentConstructor를 사용해서 전체 컬럼 명시 후 생성
        );
    }
}
