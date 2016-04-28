/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import scala.collection.immutable.TreeMap
import scala.util.Random

class Merchant {

  val r = Random

  val mktList = TreeMap[Double, String](
                      0.180678812734969 -> "Wal-Mart Stores",
                      0.234853957988275 -> "The Kroger Co.",
                      0.276757367826064 -> "Costco",
                      0.315773588873991 -> "The Home Depot",
                      0.353984278465704 -> "Walgreen",
                      0.39216710045482 -> "Target",
                      0.427908089491811 -> "CVS Caremark",
                      0.456724767989063 -> "Lowe's Companies",
                      0.482674764045535 -> "Amazon.com",
                      0.501777216920367 -> "Safeway",
                      0.520683544969372 -> "Best Buy",
                      0.539321713068854 -> "McDonald's",
                      0.55539027788732 -> "Publix Super Markets",
                      0.570312590372532 -> "Apple Store / iTunes",
                      0.585049294108368 -> "Macy's",
                      0.598997817914136 -> "Rite Aid",
                      0.612656098009832 -> "Royal Ahold / Ahold USA",
                      0.626202381891316 -> "Sears Holdings",
                      0.637878381575834 -> "TJX",
                      0.64829928753582 -> "H-E-B Grocery",
                      0.658671819544128 -> "YUM! Brands",
                      0.668902910324158 -> "Albertsons",
                      0.678905276441359 -> "Kohl's",
                      0.688848226726608 -> "Dollar General",
                      0.697823172174461 -> "Delhaize America",
                      0.706072508347135 -> "Meijer",
                      0.713951678628703 -> "WakeFern / ShopRite",
                      0.721470147487972 -> "Ace Hardware",
                      0.728732024081815 -> "BJ's Wholesale Club",
                      0.735905039829639 -> "Whole Foods Market",
                      0.742945027210347 -> "Doctor's Assoc. / Subway",
                      0.749916660094119 -> "Nordstrom",
                      0.756789441859242 -> "Gap",
                      0.763603859400058 -> "AT&T Wireless",
                      0.77001025317454 -> "J.C. Penney Co.",
                      0.77617688040592 -> "Aldi",
                      0.782332991560848 -> "Bed Bath & Beyond",
                      0.788379209716854 -> "SUPERVALU",
                      0.794368115256198 -> "7-Eleven",
                      0.800168783027052 -> "Ross Stores",
                      0.805931067118858 -> "Verizon Wireless",
                      0.811506690853642 -> "Starbucks",
                      0.817021847148829 -> "Family Dollar Stores",
                      0.822470226358545 -> "Bi-Lo",
                      0.827887583142729 -> "L Brands",
                      0.83298209637984 -> "Menard",
                      0.837918342666351 -> "Trader Joe's",
                      0.842590635433919 -> "Wendy's",
                      0.847068906590951 -> "Burger King Worldwide",
                      0.851480400662513 -> "Dollar Tree",
                      0.855631621841891 -> "Hy-Vee",
                      0.859700291821121 -> "Army / Air Force Exchange",
                      0.863759497331545 -> "Dunkin' Brands Group",
                      0.867767699871178 -> "Health Mart Systems",
                      0.871723322028551 -> "AutoZone",
                      0.875640560506875 -> "Toys R Us",
                      0.879534137813182 -> "Wegmans Food Market",
                      0.883328338197018 -> "O'Reilly Automotive",
                      0.88708100007887 -> "DineEquity",
                      0.890791071851092 -> "Giant Eagle",
                      0.894496411388911 -> "Sherwin-Williams",
                      0.898077661224597 -> "Dick's Sporting Goods",
                      0.901551647080474 -> "Staples",
                      0.905015116859899 -> "Office Depot",
                      0.908427583668533 -> "Dillard's",
                      0.91180008938665 -> "Good Neighbor Pharmacy",
                      0.915091095512264 -> "Darden Restaurants",
                      0.91836422430791 -> "GameStop",
                      0.921557430922523 -> "PetSmart",
                      0.924697531351053 -> "QVC",
                      0.927818702841969 -> "Chick-fil-A",
                      0.93090622288824 -> "WinCo Foods",
                      0.933966401135736 -> "Tractor Supply Co.",
                      0.937010805268554 -> "Barnes & Noble",
                      0.939971080789757 -> "A&P",
                      0.942741015327181 -> "AVB Brandsource",
                      0.945502011199621 -> "Signet Jewelers",
                      0.94825669742619 -> "Foot Locker",
                      0.950978783815758 -> "Big Lots",
                      0.953699292793858 -> "Hudson's Bay",
                      0.9564161211452 -> "Alimentation Couche-Tard",
                      0.958987301837684 -> "Defense Commissary Agency",
                      0.961523253674054 -> "Neiman Marcus",
                      0.964037121749875 -> "Jack in the Box",
                      0.966515235165759 -> "Ascena Retail Group",
                      0.968990193758708 -> "Burlington Coat Factory",
                      0.971414675184688 -> "Ikea North America Services",
                      0.973828640534216 -> "Williams-Sonoma",
                      0.976204222204695 -> "Save Mart Supermarkets",
                      0.978563503956673 -> "Panera Bread Company",
                      0.980865998895812 -> "Advance Auto Parts",
                      0.983114861845045 -> "Michaels Stores",
                      0.985279596182664 -> "True Value Co.",
                      0.98744380471646 -> "Domino's Pizza",
                      0.989604858427321 -> "Belk",
                      0.991744354181455 -> "Chipotle Mexican Grill",
                      0.993864920997975 -> "Sonic",
                      0.995920813944317 -> "Stater Bros. Holdings",
                      0.997966190814207 -> "Price Chopper Supermarkets",
                      1.0 -> "Dell"
  )

  def nextMerchant() : String = {
    val testVal : Double = r.nextDouble()

    for ((key, value) <- mktList) { if (key >= testVal) { return value } }

    return "All Other"
  }

}
