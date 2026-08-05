import { HSCSubjectMark } from '../types';

/**
 * New Cut-Off Formula (out of 300):
 * Cut-Off = Maths (100) + Physics (100) + 3rd Subject (100)
 * Where 3rd Subject = Biology / Computer Science / Bio Technology / Chemistry
 */
export const calculateHSCCutOff = (marks: HSCSubjectMark[], stream: 'Academic' | 'Vocational'): number => {
  if (!marks || marks.length === 0) return 0;

  const getPercentage = (mark: HSCSubjectMark): number => {
    if (!mark || !mark.maxMarks || mark.maxMarks <= 0) return 0;
    return Number(((mark.marksObtained / mark.maxMarks) * 100).toFixed(2));
  };

  if (stream === 'Academic') {
    // Formula: Maths (100) + Physics (100) + 3rd Subject (100) = 300
    const maths = marks.find((m) => m.subject.toLowerCase().includes('maths'));
    const physics = marks.find((m) => m.subject.toLowerCase().includes('physics'));
    // 3rd subject: Biology, Computer Science, Bio Technology, or Chemistry
    const thirdSubject = marks.find((m) =>
      m.subject.toLowerCase().includes('biology') ||
      m.subject.toLowerCase().includes('computer') ||
      m.subject.toLowerCase().includes('bio technology') ||
      m.subject.toLowerCase().includes('biotechnology') ||
      m.subject.toLowerCase().includes('chemistry')
    );

    const mathsPct = maths ? getPercentage(maths) : 0;
    const physicsPct = physics ? getPercentage(physics) : 0;
    const thirdPct = thirdSubject ? getPercentage(thirdSubject) : 0;

    const cutoff = mathsPct + physicsPct + thirdPct;
    return Number(cutoff.toFixed(2));
  } else {
    // Vocational Formula: Vocational Subject (100) + Related I (100) + Related II (100) = 300
    const vocational = marks.find((m) => m.subject.toLowerCase().includes('vocational'));
    const related1 = marks.find((m) => m.subject.toLowerCase().includes('related subject i'));
    const related2 = marks.find((m) => m.subject.toLowerCase().includes('related subject ii'));

    const vocPct = vocational ? getPercentage(vocational) : 0;
    const r1Pct = related1 ? getPercentage(related1) : 0;
    const r2Pct = related2 ? getPercentage(related2) : 0;

    const cutoff = vocPct + r1Pct + r2Pct;
    return Number(cutoff.toFixed(2));
  }
};
