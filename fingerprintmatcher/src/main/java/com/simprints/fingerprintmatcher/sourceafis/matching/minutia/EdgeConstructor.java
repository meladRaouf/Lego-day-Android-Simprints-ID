package com.simprints.fingerprintmatcher.sourceafis.matching.minutia;

import com.simprints.fingerprintmatcher.sourceafis.general.Angle;
import com.simprints.fingerprintmatcher.sourceafis.general.Calc;
import com.simprints.fingerprintmatcher.sourceafis.general.PolarPoint;
import com.simprints.fingerprintmatcher.sourceafis.templates.Template;
/*
 *  Call to Construct can be static and no need to create EdgeConstructor object
 */
public final class EdgeConstructor
{
	   public EdgeShape Construct(Template template, int reference, int neighbor)
       {
           PolarPoint polar = Angle.ToPolar(Calc.Difference(template.minutiae[neighbor].Position, template.minutiae[reference].Position));
           EdgeShape edge=new EdgeShape();
           edge.length = (short)polar.Distance;
           edge.referenceAngle = Angle.Difference(template.minutiae[reference].Direction, polar.Angle);
           edge.neighborAngle = Angle.Difference(template.minutiae[neighbor].Direction, Angle.Opposite(polar.Angle));
           return edge;
       }
}