package com.rgcet.admission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "parent_details")
@Getter
@Setter
@NoArgsConstructor
public class ParentDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "parent_id")
    private Long parentId;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @Column(name = "father_name")
    private String fatherName;

    @Column(name = "father_mobile_no")
    private String fatherMobileNo;

    @Column(name = "father_occupation")
    private String fatherOccupation;

    @Column(name = "annual_income")
    private BigDecimal annualIncome;
}
