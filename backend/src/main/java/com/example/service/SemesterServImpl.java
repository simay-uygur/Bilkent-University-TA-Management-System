package com.example.service;


import com.example.entity.General.Semester;
import com.example.entity.General.Term;
import com.example.repo.SemesterRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SemesterServImpl implements SemesterServ {
    private final SemesterRepo repo;
    private final LogService log;
    @Override
    public Semester create(Semester semester) {
        Optional<Semester> existing = repo.findByYearAndTerm(semester.getYear(), semester.getTerm());
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Semester for year " + semester.getYear() + " and term " + semester.getTerm() + " already exists.");
        }
        log.info("Semested is created", "");
        return repo.save(semester);
    }

    @Override
    public Semester update(Integer id, Semester semester) {
        Semester existing = getById(Long.valueOf(id));
        existing.setTerm(semester.getTerm());
        existing.setYear(semester.getYear());
        existing.setOfferings(semester.getOfferings());
        log.info("Semested Updation", "Semester with id: " + id + " is updated");
        return repo.save(existing);
    }

    @Override
    public Semester getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Semester not found: " + id));
    }

    @Override
    public List<Semester> getAll() {
        return repo.findAll();
    }

    @Override
    public void delete(Long id) {
        log.info("Semested Deletion", "Semester with id: " + id + " is deleted");
        repo.deleteById(id);
    }

    @Override
    public Optional<Semester> findByYearAndTerm(int year, String term) {
        return repo.findByYearAndTerm(year, Term.valueOf(term));
    }
}
