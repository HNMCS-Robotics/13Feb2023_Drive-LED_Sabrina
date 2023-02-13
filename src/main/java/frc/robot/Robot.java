// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

/**
 * AUTHOR: Sabrina Fang
 * SUBJECT: ICS4U
 * DATE: February 13, 2023
 * PROJECT TITLE: Tele-Op Drive and LED lights control with Joystick controllers
 
 * This is v.10Feb2023 but updated to have a different LED lights colour for each button AND
 * fixed code so the controller joystick and movement of the robot makes sense (EX: left on the controller to go left) 
 */

package frc.robot;

//DEFAULT TEMPLATE LIBRARIES
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

//IMPORT DRIVE LIBRARIES - ALL added on January 10
import edu.wpi.first.wpilibj.motorcontrol.PWMVictorSPX; //give PWM controll to Vicor SPX motor
import edu.wpi.first.wpilibj.drive.DifferentialDrive; //Class for Differential Drive - motors of the wheels on the left and right
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup; //links motor controller objects together EX: grouping all motors of the LEFT side together

//IMPORT JOYSTICK LIB - Added on Jan 9 
import edu.wpi.first.wpilibj.Joystick;

//IMPORT THE SPARK WPI (LED LIGHTS) Lib
import edu.wpi.first.wpilibj.motorcontrol.Spark;

/**
 * The VM is configured to automatically run this class, and to call the functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the name of this class or
 * the package after creating this project, you must also update the build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  private static final String kDefaultAuto = "Default"; 
  private static final String kCustomAuto = "My Auto";
  private String m_autoSelected;
  private final SendableChooser<String> m_chooser = new SendableChooser<>(); //obj

  //Initialize LED Lights variables
  private static Spark ledLights; //Spark motor controller

  //Initialize Basic Drive
  private final double maxSpeed=0.65; //with boost - RUN SPEED 0.75
  private final double maxTurn=0.65; //with boost - RUN trun 0.70
  private final double boost =0.25;

  private double variableSpeed = maxSpeed;

  //Initialize objects with Motors of each port
  private final PWMVictorSPX m_LeftMotorControlFront = new PWMVictorSPX(1); //Left-Front is in port 1
  private final PWMVictorSPX m_LeftMotorControlRear = new PWMVictorSPX(0); //Left-Rear is in port 0
  private final PWMVictorSPX m_RightMotorControlFront = new PWMVictorSPX(2); //Right-Front is in port 2
  private final PWMVictorSPX m_RightMotorControlRear = new PWMVictorSPX(3); //Right-Rear is in port 3

  //Grouping motors
  private final MotorControllerGroup m_Left = new MotorControllerGroup(m_LeftMotorControlFront, m_LeftMotorControlRear); //grouping all motors on the LEFT 
  private final MotorControllerGroup m_Right = new MotorControllerGroup(m_RightMotorControlFront, m_RightMotorControlRear); //grouping all motors on the RIGHT
  
  //Initialize Differential Drive
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_Left, m_Right); //allows each side to be independently moving


  //Initialize Joystick Controllers
  private final Joystick stickDrive = new Joystick (0); //initialize Driving joystick controller
  private final Joystick stickOperator = new Joystick (1); //initialize Operating joystick controller

  /**
   * This function is run when the robot is first started up and should be used for any
   * initialization code.
   */
  @Override
  public void robotInit() {
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("My Auto", kCustomAuto);
    SmartDashboard.putData("Auto choices", m_chooser);

    //Initialize led lights
    ledLights = new Spark(9); //ledLights on robotRio PWM port 9 
    ledLights.set(0.75); //set PWM between -1,00 to 1.00 

  }
 
  /**
   * This function is called every 20 ms, no matter the mode. Use this for items like diagnostics
   * that you want ran during disabled, autonomous, teleoperated and test.
   *
   * <p>This runs after the mode specific periodic functions, but before LiveWindow and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {}

  /**
   * This autonomous (along with the chooser code above) shows how to select between different
   * autonomous modes using the dashboard. The sendable chooser code works with the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the chooser code and
   * uncomment the getString line to get the auto name from the text box below the Gyro
   *
   * <p>You can add additional auto modes by adding additional comparisons to the switch structure
   * below with additional strings. If using the SendableChooser make sure to add them to the
   * chooser code above as well.
   */
  @Override
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: Sabrina" + m_autoSelected);
    System.out.println("Led Lights - Feb 6th, 2023");
  }

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto: // Custom Auto Routine - "My Auto"
        // Put custom auto code here
        ledLights.set(0.87); //Set PWM to 0.87 which is blue
        break;
      case kDefaultAuto: //Default Auto Routine - "Default"
      default:
        ledLights.set(0.91); //set PWM to 0.91 which is purple
        break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  public void teleopInit() {}

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    /******
    BUTTONS - the button diagram in 2023_02_07 ChargedUp is incorrect (Switched 2 and 3 )
            5         6
                 4
              3     2
                 1
    **********INTAKE is FRONT, SHOOTER is the BACK*************
     */
    
    //Drive Control
    if (stickDrive.getRawAxis(3)>0){
      variableSpeed = maxSpeed+boost*stickDrive.getRawAxis(3); // getRawAxis(3) is Right Trigger; For Boost
    }else{
      variableSpeed = maxSpeed; //no boost is maxSpeed
    };

    //Arcade class expects parameters (TURN, Forward) (axis(1) axis(0) (x, y))
    //                      (        this is parameter for TURN        ),  (  this is parameter for FORWARD     )
    m_robotDrive.arcadeDrive(variableSpeed*stickDrive.getRawAxis(0), maxTurn*stickDrive.getRawAxis(1)); 
    //Removed -1 to get the correct turns
    //Changed to RawAxis for TURN to 0 so moving joyStick left and right (0: LX Axis) is turning
    //Changed to RawAxis for FORWARD to 1 (1: LY Axis) so up is forward 

    //Operating Control
    if (stickOperator.getRawButton(1)==true) { //button 1 (Green button)
      ledLights.set(0.77); //GREEN
    } else if (stickOperator.getRawButton(2)==true){ //button 2 (Red Button)
        ledLights.set(0.61); //RED
    } else if (stickOperator.getRawButton(3)==true){ //button 3 (Blue Button)
      ledLights.set(0.85); //BLUE
    } else if (stickOperator.getRawButton(4)==true){ //button 4 (Yellow Button)
      ledLights.set(0.69); //YELLOW
    } else if (stickOperator.getRawButton(5)==true){ //button 5 (Left Trigger)
      ledLights.set(0.57); //PINK
    } else if (stickOperator.getRawButton(6)==true){ //button 6 (Right Trigger);
      ledLights.set(0.91); //PURPLE
    } else { //if all the conditions are false
      ledLights.set(0.93); //WHITE
    }
  }

  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {}

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {}

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {}

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {}

  /** This function is called once when the robot is first started up. */
  @Override
  public void simulationInit() {}

  /** This function is called periodically whilst in simulation. */
  @Override
  public void simulationPeriodic() {}
}
