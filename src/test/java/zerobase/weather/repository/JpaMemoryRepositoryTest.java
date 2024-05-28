package zerobase.weather.repository;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import zerobase.weather.domain.Memo;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional // 테스트 코드에서 사용하면, 모두 롤백처리
class JpaMemoryRepositoryTest {

    @Autowired
    JpaMemoRepository jpaMemoRepository;

    @Test
    void insertMemoTest() {
        //given
        Memo newMemo = new Memo(10, "this is jpa memo");
        //when
        jpaMemoRepository.save(newMemo); // jpaMemoryRepository가 save 메소드를 제공
        //then
        List<Memo> memoList = jpaMemoRepository.findAll();
        assertFalse(memoList.isEmpty()); // 내용이 비어 있다는 것이 거짓인지 테스트
    }

    @Test
    void findById () {
        //given
        Memo newMemo = new Memo(11, "another jpa test memo"); // 사실상 id 지정은 의미 없음
        //when
        Memo memo = jpaMemoRepository.save(newMemo);
        //then
        Optional<Memo> result = jpaMemoRepository.findById(memo.getId()); // DB에서 자동으로 만들어진 id를 불러와서 사용해야 함
        assertTrue(result.isPresent(), "Memo should be present");
        assertEquals(result.get().getText(), "another jpa test memo");
    }
}