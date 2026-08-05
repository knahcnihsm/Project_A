package com.rgcet.admission.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "student_fee")
@Getter
@Setter
@NoArgsConstructor
public class StudentFee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "student_fee_id")
    private Long studentFeeId;

    @OneToOne
    @JoinColumn(name = "student_id")
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_structure_id")
    private TuitionFeeStructure feeStructure;

    @Column(name = "cut_off_mark")
    private BigDecimal cutOffMark;

    @Column(name = "tuition_fee_per_year")
    private BigDecimal tuitionFeePerYear;

    @Column(name = "course_duration_years")
    private Integer courseDurationYears;

    @Column(name = "total_tuition_fee")
    private BigDecimal totalTuitionFee;

    @Column(name = "bus_required")
    private Boolean busRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "route_id")
    private BusRoute route;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bus_stop_id")
    private BusStop busStop;

    @Column(name = "bus_fee")
    private BigDecimal busFee;

    @Column(name = "hostel_required")
    private Boolean hostelRequired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hostel_id")
    private Hostel hostel;

    @Column(name = "hostel_fee")
    private BigDecimal hostelFee;

    @Column(name = "total_fee")
    private BigDecimal totalFee;

    @Column(name = "paid_amount")
    private BigDecimal paidAmount;

    @Column(name = "pending_amount")
    private BigDecimal pendingAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus;
}
