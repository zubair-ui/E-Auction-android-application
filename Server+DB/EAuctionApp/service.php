<?php
    include 'config/db_config.php';

    $data = file_get_contents("php://input");

    $request = json_decode($data);

    $response = array();

    $isValidRequest = false;

    if(isset($request->{'action'})){
        if ($request->{'action'} == 'SIGN_UP') {
            $isValidRequest = true;    
            $userName = $request->{'username'};
            $email = $request->{'userEmail'};
            $password = $request->{'password'};

            
            $checkQuery = "SELECT * FROM `user` WHERE userEmail = '".$email."'";
            $checkResult = mysqli_query($connection, $checkQuery);

            if ($checkResult && mysqli_num_rows($checkResult) > 0) {
                
                $response['status'] = false;
                $response['responseCode'] = 1; 
                $response['message'] = "User already exists";
            } else {
                
                $insertQuery = "INSERT INTO `User`(`userEmail`, `username`, `password`) VALUES('".$email."', '".$userName."', '".$password."')";
                $insertResult = mysqli_query($connection, $insertQuery);
                if ($insertResult) {
                    $response['status'] = true; 
                    $response['responseCode'] = 0; 
                    $response['message'] = "User has been added";
                } else {
                    $response['status'] = false; 
                    $response['responseCode'] = 102; 
                    $response['message'] = "User registration failed";
                }
            }
        }

        if ($request->{'action'} == 'LOG_IN') {
            $isValidRequest = true;    
            $email = $request->{'userEmail'};
            $password = $request->{'password'};

            
            $checkQuery = "SELECT * FROM `User` WHERE userEmail = '".$email."' AND password = '".$password."'";
            $checkResult = mysqli_query($connection, $checkQuery);

            if ($checkResult && mysqli_num_rows($checkResult) > 0) {
                $response['status'] = true;
                $response['responseCode'] = 0; 
                $response['message'] = "User logged in successfully";
            } else {
                $response['status'] = false; 
                $response['responseCode'] = 103; 
                $response['message'] = "Invalid email or password";
            }
        }

        if ($request->{'action'} == 'ADD_BID_ITEM') {
            $isValidRequest = true;    
            $ownerEmail = $request->{'userEmail'};
            $title = $request->{'title'};
            $description = $request->{'description'};
            $image = $request->{'image'};
            $startingBid = $request->{'startingBid'};
            $startDate = $request->{'startDate'};
            $endDate = $request->{'endDate'};

            $highestBidderEmail = "N/A";  

    		$query = "INSERT INTO BidItem(`ownerEmail`, `title`, `description`, `image`, `startingBid`, `highestBid`, `startDate`, `endDate`, `highestBidderEmail`) VALUES('".$ownerEmail."', '".$title."', '".$description."', '".$image."', '".$startingBid."', 0, '".$startDate."', '".$endDate."', '".$highestBidderEmail."')";
    		$result = mysqli_query($connection, $query);
            if ($result) {
                $response['bidId'] = mysqli_insert_id($connection);
                $response['status'] = true; 
                $response['responseCode'] = 0; 
                $response['message'] = "Bid item added successfully";
            } else {
                $response['status'] = false; 
                $response['responseCode'] = 105; 
                $response['message'] = "Bid item addition failed";
            }
        }

        if ($request->{'action'} == 'UPDATE_BID_ITEM') {
            $isValidRequest = true;    
            $bidId= $request->{'bidId'};
            $ownerEmail = $request->{'userEmail'};
            $title = $request->{'title'};
            $description = $request->{'description'};
            $image = $request->{'image'};
            $startingBid = $request->{'startingBid'};
            $startDate = $request->{'startDate'};
            $endDate = $request->{'endDate'}; 

    		$query = "UPDATE BidItem SET title='".$title."', description='".$description."', image='".$image."', startingBid='".$startingBid."', startDate='".$startDate."', endDate='".$endDate."' WHERE bidId='".$bidId."'";
    		$result = mysqli_query($connection, $query);
            if ($result) {
                $response['bidId'] = $bidId;
                $response['status'] = true; 
                $response['responseCode'] = 0; 
                $response['message'] = "Bid item updated successfully";
            } else {
                $response['status'] = false; 
                $response['responseCode'] = 105; 
                $response['message'] = "Bid item updation failed";
            }
        }

        if ($request->{'action'} == 'GET_BIDS') {
            $isValidRequest = true;    
            $userEmail = $request->{'userEmail'};

            
            $userBids = array();
            $usersHighestBids = array();
            $previousBids = array();
            $currentBids = array();
            $futureBids = array();

            
            $query = "SELECT * FROM BidItem";
            $result = mysqli_query($connection, $query);

            if ($result && mysqli_num_rows($result) > 0) {
                while ($row = mysqli_fetch_assoc($result)) {
                    
                    $currentDate = gmdate('Y-m-d', time());

                    if ($row['ownerEmail'] == $userEmail) {
                        $userBids[] = $row;
                    } else if ($row['endDate'] < $currentDate) {
                        $previousBids[] = $row;
                    } else if ($row['startDate'] > $currentDate) {
                        $futureBids[] = $row;
                    } else {
                        if ($row['highestBidderEmail'] == $userEmail) {
                            $usersHighestBids[] = $row;
                        } else {
                            $currentBids[] = $row;
                        }
                    }
                }

                $response['status'] = true; 
                $response['responseCode'] = 0; 
                $response['message'] = "Bids are available";
                $response['userBids'] = $userBids;
                $response['usersHighestBids'] = $usersHighestBids;
                $response['previousBids'] = $previousBids;
                $response['currentBids'] = $currentBids;
                $response['futureBids'] = $futureBids;
            } else {
                $response['status'] = false; 
                $response['responseCode'] = 104; 
                $response['message'] = "Bids are not available";
            }
        }

        if ($request->{'action'} == 'UPDATE_HIGHEST_BIDDER') {
            $isValidRequest = true;    
            $bidId = $request->{'bidId'};
            $highestBidderEmail = $request->{'userEmail'};
            $highestBid = $request->{'highestBid'};

            $query = "UPDATE BidItem SET highestBidderEmail='".$highestBidderEmail."', highestBid='".$highestBid."' WHERE bidId='".$bidId."'";
            $result = mysqli_query($connection, $query);
            if ($result) {
                $response['bidId'] = $bidId;
                $response['status'] = true; 
                $response['responseCode'] = 0; 
                $response['message'] = "Highest bidder updated successfully";
            } else {
                $response['status'] = false; 
                $response['responseCode'] = 107; 
                $response['message'] = "Update highest bidder failed";
            }
        }

        if ($request->{'action'} == 'DELETE_BID_ITEM') {
            $isValidRequest = true;    
            $bidId = $request->{'bidId'};
            $ownerEmail = $request->{'userEmail'};

            $query = "DELETE FROM BidItem WHERE bidId='".$bidId."' AND ownerEmail='".$ownerEmail."'";
            $result = mysqli_query($connection, $query);
            if ($result) {
                $response['bidId'] = $bidId;
                $response['status'] = true; 
                $response['responseCode'] = 0; 
                $response['message'] = "Bid item deleted successfully";
            } else {
                $response['status'] = false; 
                $response['responseCode'] = 106; 
                $response['message'] = "Bid item deletion failed";
            }
        }

        if(!$isValidRequest){
            $response['status'] = false; 
            $response['responseCode'] = 101; 
            $response['message'] = "Invalid request action";
        }
    } else {
        $response['status'] = false; 
        $response['responseCode'] = 100; 
        $response['message'] = "Request action not defined";
    }

    echo json_encode($response);
?>
