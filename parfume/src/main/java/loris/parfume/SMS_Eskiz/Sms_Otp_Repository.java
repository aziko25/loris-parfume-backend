package loris.parfume.SMS_Eskiz;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Sms_Otp_Repository extends JpaRepository<Sms_Otp, Long> {
}