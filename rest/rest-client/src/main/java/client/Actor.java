package client;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by jlong on 5/24/15.
 */
@Entity
class Actor {

    @Id
    @GeneratedValue
    public Long id;

    @ManyToOne
    public Movie movie;

    public String fullName;

    public Actor(String n, Movie movie) {
        this.fullName = n;
        this.movie = movie;
    }

    Actor() {
    }

    @Override
    public String toString() {
        return "Actor{" +
                "id=" + id +
                ", fullName='" + this.fullName + '\'' +
                '}';
    }
}
