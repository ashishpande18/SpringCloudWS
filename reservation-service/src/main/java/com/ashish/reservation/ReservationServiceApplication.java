package com.ashish.reservation;

import java.util.Arrays;
import java.util.Collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.cloud.stream.annotation.EnableBinding;
import org.springframework.cloud.stream.messaging.Sink;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.integration.annotation.MessageEndpoint;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@EnableBinding(Sink.class)
@EnableDiscoveryClient
@SpringBootApplication
public class ReservationServiceApplication {

	
	@Bean
	CommandLineRunner runner(ReservationRepository rr){
		return args ->{ Arrays.asList("A,B,C,D,E,F,G,H,I".split(","))
				.forEach(x->rr.save(new Reservation(x)));
				rr.findAll().forEach(System.out::println);
		};	
		
	}
	
	public static void main(String[] args) {
		SpringApplication.run(ReservationServiceApplication.class, args);
	}
}


@MessageEndpoint
class MessageReservationReceiver{
	
	@ServiceActivator(inputChannel=Sink.INPUT)
	public void acceptReservation(String r){
		reservationRepository.save(new Reservation(r));
	}
	
	@Autowired
	private ReservationRepository reservationRepository;
}



@RefreshScope
@RestController
class TestRestController{
	
	@Value("${test}")
	private String testMessage;
	
	@RequestMapping("/testmessage")
	public String message(@RequestParam("name") String name){
		return testMessage+": "+name;
	}
}

@RepositoryRestResource
interface ReservationRepository extends JpaRepository<Reservation, Long>{
	@RestResource(path="by-name")
	Collection<Reservation> findByReservationName(@Param("rn") String rn);
}

@Entity
class Reservation {
	
	public Reservation(String reservationName) {
		super();
		this.reservationName = reservationName;
	}
	
	public Reservation(){}


	@Id
	@GeneratedValue
	private Long ID;
	
	@Column
	private String reservationName;
	
	public String getReservationName() {
		return reservationName;
	}


	public void setReservationName(String reservationName) {
		this.reservationName = reservationName;
	}


	public Long getID() {
		return ID;
	}
	
	@Override
	public String toString() {
		return "Reservation [ID=" + ID + ", reservationName=" + reservationName + "]";
	}
	
}
