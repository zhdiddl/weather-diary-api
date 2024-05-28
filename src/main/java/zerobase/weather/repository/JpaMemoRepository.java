package zerobase.weather.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import zerobase.weather.domain.Memo;

@Repository
public interface JpaMemoRepository extends JpaRepository<Memo, Integer> {
}
// JpaRepository<관리할 엔티티 클래스명, 해당 엔티티 기본키 타입>